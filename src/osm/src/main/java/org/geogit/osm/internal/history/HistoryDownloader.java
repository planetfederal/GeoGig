/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.osm.internal.history;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.io.Closeables;

/**
 *
 */
public class HistoryDownloader {

    private final long initialChangeset;

    private final long finalChangeset;

    private long currChangeset;

    private final ChangesetDownloader downloader;

    private boolean started;

    private final boolean preserveFiles;

    private Predicate<Changeset> filter = Predicates.alwaysTrue();

    /**
     * @param osmAPIUrl api url, e.g. {@code http://api.openstreetmap.org/api/0.6},
     *        {@code file:/path/to/downloaded/changesets}
     * @param initialChangeset initial changeset id
     * @param finalChangeset final changeset id
     * @param preserveFiles
     */
    public HistoryDownloader(final String osmAPIUrl, final File downloadFolder,
            long initialChangeset, long finalChangeset, ExecutorService executor,
            boolean preserveFiles) {

        checkArgument(initialChangeset > 0 && initialChangeset <= finalChangeset);

        this.initialChangeset = initialChangeset;
        this.finalChangeset = finalChangeset;
        this.preserveFiles = preserveFiles;
        this.downloader = new ChangesetDownloader(osmAPIUrl, downloadFolder, executor);
        currChangeset = this.initialChangeset;
    }

    public void setFilter(Predicate<Changeset> filter) {
        this.filter = filter;
    }

    private BlockingQueue<Changeset> changesetsQueue = new ArrayBlockingQueue<Changeset>(100);

    /**
    *
    */
    private class ChangesSupplier implements Supplier<Optional<Iterator<Change>>> {

        private Supplier<Optional<File>> changesFile;

        /**
         * @param changesFile2
         */
        public ChangesSupplier(Supplier<Optional<File>> changesFile) {
            this.changesFile = changesFile;
        }

        @Override
        public Optional<Iterator<Change>> get() {
            return parseChanges(changesFile);
        }

    }

    /**
     * @return the next available changeset, or absent if reached the last one
     * @throws IOException
     * @throws InterruptedException
     */
    public Optional<Changeset> fetchNextChangeset() {
        if (!started) {
            started = true;
            Thread runner = new Thread() {
                @Override
                public void run() {
                    for (long changeset = initialChangeset; changeset <= finalChangeset; changeset++) {
                        Supplier<Optional<File>> changesetFile;
                        changesetFile = downloader.fetchChangeset(changeset);

                        Optional<Changeset> set = parseChangeset(changesetFile);

                        Changeset changeSet = set.get();

                        if (filter.apply(changeSet)) {
                            Supplier<Optional<File>> changesFile;
                            changesFile = downloader.fetchChanges(changeset);
                            Supplier<Optional<Iterator<Change>>> changes = new ChangesSupplier(
                                    changesFile);
                            changeSet.setChanges(changes);
                        }

                        try {
                            // put the element on the queue, blocking until space is available
                            // if necessary
                            changesetsQueue.put(changeSet);
                        } catch (InterruptedException e) {
                            System.out.println(Thread.currentThread().getName()
                                    + " interrupted. Exiting gracefully. "
                                    + "No more changes will be queued.");
                        }

                    }
                }
            };
            runner.setName("OSM History download consumer");
            runner.setDaemon(true);
            runner.start();
        }

        Changeset next = null;

        while (currChangeset <= finalChangeset && next == null) {
            Changeset cs;
            try {
                cs = changesetsQueue.poll(30, TimeUnit.SECONDS);
                if (cs == null) {
                    String msg = "Waited for next changeset for 30 seconds, aborting";
                    System.err.println(msg);
                    throw new RuntimeException(msg);
                }
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
            currChangeset++;
            try {
                next = cs;
            } catch (RuntimeException e) {
                if (e.getCause() instanceof FileNotFoundException) {
                    continue;
                }
                throw Throwables.propagate(e.getCause());
            }
        }

        return Optional.fromNullable(next);
    }

    private Optional<Changeset> parseChangeset(Supplier<Optional<File>> file) {

        Optional<File> changesetFile;
        try {
            changesetFile = file.get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                Optional.absent();
            }
            throw Throwables.propagate(e.getCause());
        }

        if (!changesetFile.isPresent()) {
            Optional.absent();
        }
        Changeset changeset = null;

        InputStream stream = null;
        try {
            final File actualFile = changesetFile.get();
            stream = new BufferedInputStream(new FileInputStream(actualFile), 4096);
            Optional<Changeset> cs;
            try {
                cs = new ChangesetScanner().parse(stream);
            } catch (XMLStreamException e) {
                throw Throwables.propagate(e);
            }
            if (cs.isPresent()) {
                changeset = cs.get();
                if (!preserveFiles) {
                    actualFile.delete();
                }
            }
        } catch (FileNotFoundException e) {
            throw Throwables.propagate(e);
        } finally {
            Closeables.closeQuietly(stream);
        }

        return Optional.fromNullable(changeset);
    }

    private Optional<Iterator<Change>> parseChanges(Supplier<Optional<File>> file) {

        final Optional<File> changesFile;
        try {
            changesFile = file.get();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FileNotFoundException) {
                return Optional.absent();
            }
            throw Throwables.propagate(e);
        }
        if (!changesFile.isPresent()) {
            return Optional.absent();
        }
        final File actualFile = changesFile.get();
        final InputStream stream = openStream(actualFile);
        final Iterator<Change> changes;
        ChangesetContentsScanner scanner = new ChangesetContentsScanner();
        try {
            changes = scanner.parse(stream);
        } catch (XMLStreamException e) {
            throw Throwables.propagate(e);
        }

        Iterator<Change> iterator = new AbstractIterator<Change>() {
            @Override
            protected Change computeNext() {
                if (!changes.hasNext()) {
                    Closeables.closeQuietly(stream);
                    if (!preserveFiles) {
                        actualFile.delete();
                        actualFile.getParentFile().delete();
                    }
                    return super.endOfData();
                }
                return changes.next();
            }
        };
        return Optional.of(iterator);
    }

    private InputStream openStream(File file) {
        InputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(file), 4096);
        } catch (FileNotFoundException e) {
            throw Throwables.propagate(e);
        }
        return stream;
    }

}
