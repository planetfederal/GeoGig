/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Bucket;
import org.geogit.api.Node;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.RevTree;
import org.geogit.api.plumbing.diff.BoundsFilteringDiffConsumer;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.plumbing.diff.DiffPathTracker;
import org.geogit.api.plumbing.diff.DiffTreeVisitor;
import org.geogit.api.plumbing.diff.DiffTreeVisitor.Consumer;
import org.geogit.api.plumbing.diff.PathFilteringDiffConsumer;
import org.geogit.storage.ObjectDatabase;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * Compares the content and metadata links of blobs found via two tree objects on the repository's
 * {@link ObjectDatabase}
 */
public class DiffTree extends AbstractGeoGitOp<Iterator<DiffEntry>> implements
        Supplier<Iterator<DiffEntry>> {

    private final List<String> pathFilters = Lists.newLinkedList();

    private ReferencedEnvelope boundsFilter;

    private String oldRefSpec;

    private String newRefSpec;

    private boolean reportTrees;

    private boolean recursive;

    /**
     * Constructs a new instance of the {@code DiffTree} operation with the given parameters.
     */
    public DiffTree() {
        this.recursive = true;
    }

    /**
     * @param oldRefSpec the ref that points to the "old" version
     * @return {@code this}
     */
    public DiffTree setOldVersion(String oldRefSpec) {
        this.oldRefSpec = oldRefSpec;
        return this;
    }

    /**
     * @param newRefSpec the ref that points to the "new" version
     * @return {@code this}
     */
    public DiffTree setNewVersion(String newRefSpec) {
        this.newRefSpec = newRefSpec;
        return this;
    }

    /**
     * @param oldTreeId the {@link ObjectId} of the "old" tree
     * @return {@code this}
     */
    public DiffTree setOldTree(ObjectId oldTreeId) {
        this.oldRefSpec = oldTreeId.toString();
        return this;
    }

    /**
     * @param newTreeId the {@link ObjectId} of the "new" tree
     * @return {@code this}
     */
    public DiffTree setNewTree(ObjectId newTreeId) {
        this.newRefSpec = newTreeId.toString();
        return this;
    }

    /**
     * @param path the path filter to use during the diff operation, replaces any other filter
     *        previously set
     * @return {@code this}
     */
    public DiffTree setFilterPath(@Nullable String path) {
        if (path == null) {
            setFilter(null);
        } else {
            setFilter(ImmutableList.of(path));
        }
        return this;
    }

    public DiffTree setFilter(@Nullable List<String> pathFitlers) {
        this.pathFilters.clear();
        if (pathFitlers != null) {
            this.pathFilters.addAll(pathFitlers);
        }
        return this;
    }

    /**
     * Implements {@link Supplier#get()} by delegating to {@link #call()}.
     */
    @Override
    public Iterator<DiffEntry> get() {
        return call();
    }

    /**
     * Finds differences between the two specified trees.
     * 
     * @return an iterator to a set of differences between the two trees
     * @see DiffEntry
     */
    @Override
    protected Iterator<DiffEntry> _call() throws IllegalArgumentException {
        checkNotNull(oldRefSpec, "old version not specified");
        checkNotNull(newRefSpec, "new version not specified");
        final RevTree oldTree;
        final RevTree newTree;

        if (!oldRefSpec.equals(ObjectId.NULL.toString())) {
            final Optional<ObjectId> oldTreeId = command(ResolveTreeish.class).setTreeish(
                    oldRefSpec).call();
            checkArgument(oldTreeId.isPresent(), oldRefSpec + " did not resolve to a tree");
            oldTree = command(RevObjectParse.class).setObjectId(oldTreeId.get())
                    .call(RevTree.class).or(RevTree.EMPTY);
        } else {
            oldTree = RevTree.EMPTY;
        }

        if (!newRefSpec.equals(ObjectId.NULL.toString())) {
            final Optional<ObjectId> newTreeId = command(ResolveTreeish.class).setTreeish(
                    newRefSpec).call();
            checkArgument(newTreeId.isPresent(), newRefSpec + " did not resolve to a tree");
            newTree = command(RevObjectParse.class).setObjectId(newTreeId.get())
                    .call(RevTree.class).or(RevTree.EMPTY);
        } else {
            newTree = RevTree.EMPTY;
        }

        if (oldTree.equals(newTree)) {
            return Iterators.emptyIterator();
        }

        ObjectDatabase leftSource = resolveSource(oldTree.getId());
        ObjectDatabase rightSource = resolveSource(newTree.getId());
        final DiffTreeVisitor visitor = new DiffTreeVisitor(oldTree, newTree, leftSource,
                rightSource);

        final DiffEntryProducer diffProducer = new DiffEntryProducer();
        diffProducer.setReportTrees(this.reportTrees);
        diffProducer.setRecursive(this.recursive);

        Thread producerThread = new Thread() {
            @Override
            public void run() {
                Consumer consumer = diffProducer;
                if (boundsFilter != null) {
                    consumer = new BoundsFilteringDiffConsumer(boundsFilter, consumer);
                }
                if (!pathFilters.isEmpty()) {
                    consumer = new PathFilteringDiffConsumer(pathFilters, consumer);
                }

                visitor.walk(consumer);
            }
        };
        producerThread.setDaemon(true);
        producerThread.start();

        Iterator<DiffEntry> consumerIterator = new AbstractIterator<DiffEntry>() {
            @Override
            protected DiffEntry computeNext() {
                BlockingQueue<DiffEntry> entries = diffProducer.entries;
                boolean finished = diffProducer.isFinished();
                boolean empty = entries.isEmpty();
                while (!finished || !empty) {
                    try {
                        DiffEntry entry = entries.poll(10, TimeUnit.MILLISECONDS);
                        if (entry != null) {
                            return entry;
                        }
                        finished = diffProducer.isFinished();
                        empty = entries.isEmpty();
                    } catch (InterruptedException e) {
                        throw Throwables.propagate(e);
                    }
                }
                return endOfData();
            }
        };
        return consumerIterator;
    }

    private ObjectDatabase resolveSource(ObjectId treeId) {
        return objectDatabase().equals(treeId) ? objectDatabase() : stagingDatabase();
    }

    private static class DiffEntryProducer implements Consumer {

        private DiffPathTracker tracker = new DiffPathTracker();

        private boolean reportFeatures = true, reportTrees = false;

        private BlockingQueue<DiffEntry> entries = new ArrayBlockingQueue<>(10);

        private boolean finished;

        private boolean recursive = true;

        @Override
        public void feature(Node left, Node right) {
            if (this.reportFeatures) {
                String treePath = tracker.getCurrentPath();

                NodeRef oldRef = left == null ? null : new NodeRef(left, treePath, tracker
                        .currentLeftMetadataId().or(ObjectId.NULL));
                NodeRef newRef = right == null ? null : new NodeRef(right, treePath, tracker
                        .currentrightMetadataId().or(ObjectId.NULL));

                entries.offer(new DiffEntry(oldRef, newRef));
            }
        }

        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }

        public void setReportTrees(boolean reportTrees) {
            this.reportTrees = reportTrees;
        }

        public boolean isFinished() {
            return finished;
        }

        @Override
        public boolean tree(Node left, Node right) {
            final String parentPath = tracker.getCurrentPath();
            tracker.tree(left, right);
            if (reportTrees) {
                if (parentPath != null) {// do not report the root tree
                    NodeRef oldRef = left == null ? null : new NodeRef(left, parentPath, tracker
                            .currentLeftMetadataId().or(ObjectId.NULL));

                    NodeRef newRef = right == null ? null : new NodeRef(right, parentPath, tracker
                            .currentrightMetadataId().or(ObjectId.NULL));
                    entries.offer(new DiffEntry(oldRef, newRef));
                }
            }
            if (recursive) {
                return true;
            }
            return parentPath == null;
        }

        @Override
        public void endTree(Node left, Node right) {
            tracker.endTree(left, right);
            if (tracker.isEmpty()) {
                finished = true;
            }
        }

        @Override
        public boolean bucket(int bucketIndex, int bucketDepth, Bucket left, Bucket right) {
            return true;
        }

        @Override
        public void endBucket(int bucketIndex, int bucketDepth, Bucket left, Bucket right) {
            // no action required
        }
    }

    /**
     * @param reportTrees
     * @return
     */
    public DiffTree setReportTrees(boolean reportTrees) {
        this.reportTrees = reportTrees;
        return this;
    }

    /**
     * Sets whether to return differences recursively ({@code true} or just for direct children (
     * {@code false}. Defaults to {@code true}
     */
    public DiffTree setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }
}
