/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.geogit.api.Node;
import org.geogit.storage.NodeStorageOrder;
import org.geogit.storage.datastream.FormatCommonV1;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.google.common.io.Closeables;

class NodeIndex implements Closeable {

    private static final int PARTITION_SIZE = 500 * 1000;

    private static final class IndexPartition {

        List<Node> cache = new ArrayList<Node>(PARTITION_SIZE);

        public void add(Node node) {
            cache.add(node);
        }

        public List<Node> getSortedNodes() {
            Collections.sort(cache, new NodeStorageOrder());
            return cache;
        }

        public File flush() {
            List<Node> cache = getSortedNodes();
            final File file;
            try {
                file = File.createTempFile("geogitNodes", ".idx");
                file.deleteOnExit();
                // System.err.println("Created index file " + file.getAbsolutePath());
                FastByteArrayOutputStream buf = new FastByteArrayOutputStream();
                OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(file),
                        1024 * 1024);
                try {
                    for (Node node : cache) {
                        buf.reset();
                        DataOutput out = new DataOutputStream(buf);
                        try {
                            FormatCommonV1.writeNode(node, out);
                        } catch (IOException e) {
                            throw Throwables.propagate(e);
                        }
                        int size = buf.size();
                        fileOut.write(buf.bytes(), 0, size);
                    }
                } finally {
                    cache.clear();
                    fileOut.close();
                }
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
            return file;
        }
    }

    private IndexPartition currPartition;

    private List<Future<File>> indexFiles = new LinkedList<Future<File>>();

    private List<CompositeNodeIterator> openIterators = new LinkedList<CompositeNodeIterator>();

    private ExecutorService executorService;

    public NodeIndex(ExecutorService executorService) {
        this.executorService = executorService;
        this.currPartition = new IndexPartition();
    }

    public void close() {
        try {
            for (CompositeNodeIterator it : openIterators) {
                it.close();
            }
            for (Future<File> ff : indexFiles) {
                try {
                    File file = ff.get();
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            openIterators.clear();
            indexFiles.clear();
        }
    }

    public synchronized void add(Node node) {
        currPartition.add(node);
        if (currPartition.cache.size() == PARTITION_SIZE) {
            flush(currPartition);
            currPartition = new IndexPartition();
        }
    }

    private void flush(final IndexPartition ip) {
        indexFiles.add(executorService.submit(new Callable<File>() {

            @Override
            public File call() throws Exception {
                return ip.flush();
            }
        }));

    }

    public Iterator<Node> nodes() {
        List<File> files = new ArrayList<File>(indexFiles.size());
        try {
            for (Future<File> ff : indexFiles) {
                files.add(ff.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw Throwables.propagate(Throwables.getRootCause(e));
        }

        List<Node> unflushed = Lists.newArrayList(currPartition.getSortedNodes());
        currPartition.cache.clear();
        return new CompositeNodeIterator(files, unflushed);
    }

    private static class CompositeNodeIterator extends AbstractIterator<Node> {

        private NodeStorageOrder order = new NodeStorageOrder();

        private ArrayList<PeekingIterator<Node>> iterators;

        private List<IndexIterator> openIterators;

        public CompositeNodeIterator(List<File> files, List<Node> unflushedAndSorted) {

            openIterators = new ArrayList<IndexIterator>();
            iterators = new ArrayList<PeekingIterator<Node>>();
            for (File f : files) {
                IndexIterator iterator = new IndexIterator(f);
                openIterators.add(iterator);
                iterators.add(Iterators.peekingIterator(iterator));
            }
            if (!unflushedAndSorted.isEmpty()) {
                iterators.add(Iterators.peekingIterator(unflushedAndSorted.iterator()));
            }
        }

        public void close() {
            for (IndexIterator it : openIterators) {
                it.close();
            }
            openIterators.clear();
        }

        @Override
        protected Node computeNext() {
            int idx = -1;
            Node lowest = null;
            for (int i = 0; i < iterators.size(); i++) {
                PeekingIterator<Node> it = iterators.get(i);
                if (!it.hasNext()) {
                    continue;
                }
                Node peek = it.peek();
                if (lowest == null || peek == order.min(lowest, peek)) {
                    lowest = peek;
                    idx = i;
                }
            }
            return idx == -1 ? endOfData() : iterators.get(idx).next();
        }

    }

    private static class IndexIterator extends AbstractIterator<Node> {

        private DataInputStream in;

        public IndexIterator(File file) {
            Preconditions.checkArgument(file.exists(), "file %s does not exist", file);
            try {
                if (this.in == null) {
                    this.in = new DataInputStream(new BufferedInputStream(
                            new FileInputStream(file), 16 * 1024));
                }

            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        public void close() {
            Closeables.closeQuietly(in);
        }

        @Override
        protected Node computeNext() {
            try {
                Node node = FormatCommonV1.readNode(in);
                return node;
            } catch (EOFException eof) {
                Closeables.closeQuietly(in);
                return endOfData();
            } catch (Exception e) {
                Closeables.closeQuietly(in);
                throw Throwables.propagate(e);
            }
        }

    }

    private static class FastByteArrayOutputStream extends ByteArrayOutputStream {

        public FastByteArrayOutputStream() {
            super(16 * 1024);
        }

        public int size() {
            return super.count;
        }

        public byte[] bytes() {
            return super.buf;
        }
    }

}
