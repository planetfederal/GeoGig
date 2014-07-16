/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing.diff;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.locationtech.geogig.api.plumbing.diff.DiffEntry.ChangeType.ADDED;
import static org.locationtech.geogig.api.plumbing.diff.DiffEntry.ChangeType.REMOVED;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import org.locationtech.geogig.api.Bucket;
import org.locationtech.geogig.api.Node;
import org.locationtech.geogig.api.NodeRef;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.RevObject.TYPE;
import org.locationtech.geogig.api.RevTree;
import org.locationtech.geogig.api.plumbing.diff.DepthTreeIterator.Strategy;
import org.locationtech.geogig.api.plumbing.diff.DiffEntry.ChangeType;
import org.locationtech.geogig.storage.NodeStorageOrder;
import org.locationtech.geogig.storage.ObjectDatabase;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Traverses the direct children iterators of both trees (fromTree and toTree) simultaneously. If
 * the current children is named the same for both iterators, finds out whether the two children are
 * changed. If the two elements of the current iteration are not the same, find out whether it's an
 * addition or a deletion; when the change is on a subtree, returns the subtree differences before
 * continuing with the own ones.
 */
class TreeDiffEntryIterator extends AbstractIterator<DiffEntry> {

    private final ObjectDatabase objectDb;

    private Iterator<DiffEntry> delegate;

    private final boolean reportTrees;

    private final boolean recursive;

    /**
     * The {@link Strategy} used to iterate the two trees which tells whether to report or not tree
     * entries besides feature entries
     */
    private final Strategy strategy;

    public TreeDiffEntryIterator(@Nullable NodeRef oldTreeRef, @Nullable NodeRef newTreeRef,
            @Nullable RevTree oldTree, @Nullable RevTree newTree, final boolean reportTrees,
            final boolean recursive, final ObjectDatabase db) {

        checkArgument(oldTree != null || newTree != null);
        this.reportTrees = reportTrees;
        this.recursive = recursive;
        this.objectDb = db;

        this.strategy = resolveStrategy();

        if (oldTree != null && newTree != null && oldTree.getId().equals(newTree.getId())) {
            delegate = Iterators.emptyIterator();
        } else if (oldTree == null) {
            delegate = addRemoveAll(newTreeRef, newTree, ADDED);
        } else if (newTree == null) {
            delegate = addRemoveAll(oldTreeRef, oldTree, REMOVED);
        } else if (!oldTree.buckets().isPresent() && !newTree.buckets().isPresent()) {

            Strategy itStategy = recursive ? DepthTreeIterator.Strategy.CHILDREN
                    : DepthTreeIterator.Strategy.FEATURES_ONLY;

            Iterator<NodeRef> left = new DepthTreeIterator(oldTreeRef.path(),
                    oldTreeRef.getMetadataId(), oldTree, db, itStategy);

            Iterator<NodeRef> right = new DepthTreeIterator(newTreeRef.path(),
                    newTreeRef.getMetadataId(), newTree, db, itStategy);

            delegate = new ChildrenChildrenDiff(left, right);
        } else if (oldTree.buckets().isPresent() && newTree.buckets().isPresent()) {
            delegate = new BucketBucketDiff(oldTreeRef, newTreeRef, oldTree.buckets().get(),
                    newTree.buckets().get());
        } else if (newTree.buckets().isPresent()) {
            checkState(!oldTree.buckets().isPresent());
            DepthTreeIterator left = new DepthTreeIterator(oldTreeRef.path(),
                    oldTreeRef.getMetadataId(), oldTree, objectDb, strategy);

            DepthTreeIterator rightIterator;
            rightIterator = new DepthTreeIterator(newTreeRef.path(), newTreeRef.getMetadataId(),
                    newTree, objectDb, strategy);
            delegate = new ChildrenChildrenDiff(left, rightIterator);
        } else {
            checkState(oldTree.buckets().isPresent());

            DepthTreeIterator right = new DepthTreeIterator(newTreeRef.path(),
                    newTreeRef.getMetadataId(), newTree, objectDb, strategy);

            DepthTreeIterator leftIterator;
            leftIterator = new DepthTreeIterator(oldTreeRef.path(), oldTreeRef.getMetadataId(),
                    oldTree, objectDb, strategy);
            delegate = new ChildrenChildrenDiff(leftIterator, right);
            // delegate = new BucketsChildrenDiff(left, right);
        }

        // If the tree has changed its metadata Id, it will not be reported as a diff
        // up to this point.
        // We check here that both metadata Id's are identical, and if not, we add the DiffEntry
        // corresponding to the tree.
        if (reportTrees && oldTreeRef != null && newTreeRef != null
                && !oldTreeRef.getMetadataId().equals(newTreeRef.getMetadataId())) {
            DiffEntry diffEntry = new DiffEntry(oldTreeRef, newTreeRef);
            UnmodifiableIterator<DiffEntry> iter = Iterators.singletonIterator(diffEntry);
            delegate = Iterators.concat(delegate, iter);
        }
    }

    private Strategy resolveStrategy() {
        Strategy strategy;
        if (reportTrees) {
            if (recursive) {
                strategy = DepthTreeIterator.Strategy.RECURSIVE;
            } else {
                strategy = DepthTreeIterator.Strategy.CHILDREN;
            }
        } else {
            if (recursive) {
                strategy = DepthTreeIterator.Strategy.RECURSIVE_FEATURES_ONLY;
            } else {
                strategy = DepthTreeIterator.Strategy.FEATURES_ONLY;
            }
        }
        return strategy;
    }

    @Override
    protected DiffEntry computeNext() {
        if (delegate.hasNext()) {
            return delegate.next();
        }
        return endOfData();
    }

    private Iterator<DiffEntry> addRemoveAll(@Nullable final NodeRef treeRef, final RevTree tree,
            final ChangeType changeType) {
        DepthTreeIterator treeIterator;

        final String path = treeRef == null ? "" : treeRef.path();
        final ObjectId metadataId = treeRef == null ? ObjectId.NULL : treeRef.getMetadataId();

        treeIterator = new DepthTreeIterator(path, metadataId, tree, objectDb, strategy);

        Iterator<DiffEntry> iterator;

        iterator = Iterators.transform(treeIterator, new RefToDiffEntry(changeType));

        if (reportTrees && !NodeRef.ROOT.equals(path)) {
            NodeRef oldTreeRef = ChangeType.ADDED.equals(changeType) ? null : treeRef;
            NodeRef newTreeRef = ChangeType.ADDED.equals(changeType) ? treeRef : null;
            DiffEntry treeEntry = new DiffEntry(oldTreeRef, newTreeRef);
            iterator = Iterators.concat(Iterators.singletonIterator(treeEntry), iterator);
        }
        return iterator;
    }

    /**
     * Compares the contents of two leaf trees and spits out the changes. The entries must be in
     * {@link NodeRef}'s {@link NodeStorageOrder storage order}.
     * 
     */
    private class ChildrenChildrenDiff extends AbstractIterator<DiffEntry> {

        private PeekingIterator<NodeRef> left;

        private PeekingIterator<NodeRef> right;

        private Ordering<Node> comparator;

        private @Nullable
        Iterator<DiffEntry> subtreeIterator;

        public ChildrenChildrenDiff(Iterator<NodeRef> left, Iterator<NodeRef> right) {

            this.left = Iterators.peekingIterator(left);
            this.right = Iterators.peekingIterator(right);
            this.comparator = new NodeStorageOrder();
        }

        @Override
        protected DiffEntry computeNext() {
            if (null != subtreeIterator) {
                if (subtreeIterator.hasNext()) {
                    return subtreeIterator.next();
                }
                subtreeIterator = null;
            }
            if (!(left.hasNext() || right.hasNext())) {
                return endOfData();
            }

            // use peek to glimpse over the next values without consuming the iterator
            NodeRef nextLeft = left.hasNext() ? left.peek() : null;
            NodeRef nextRight = right.hasNext() ? right.peek() : null;

            if (nextLeft == null) {
                nextRight = right.next();
            } else if (nextRight == null) {
                nextLeft = left.next();
            } else if (nextLeft.path().equals(nextRight.path())) {
                // same path, consume both
                nextLeft = left.next();
                nextRight = right.next();
                if (nextLeft.equals(nextRight)) {
                    // but not a diff
                    return computeNext();
                }
            } else if (comparator.min(nextLeft.getNode(), nextRight.getNode()) == nextLeft
                    .getNode()) {
                nextLeft = left.next();
                nextRight = null;
            } else {
                nextLeft = null;
                nextRight = right.next();
            }

            final boolean isSubtree = (nextLeft != null && nextLeft.getType() == TYPE.TREE)
                    || (nextRight != null && nextRight.getType() == TYPE.TREE);

            if (isSubtree) {
                this.subtreeIterator = resolveSubtreeIterator(nextLeft, nextRight);
                return computeNext();
            }

            DiffEntry entry = new DiffEntry(nextLeft, nextRight);
            return entry;
        }

        private Iterator<DiffEntry> resolveSubtreeIterator(@Nullable NodeRef nextLeft,
                @Nullable NodeRef nextRight) {

            checkArgument(nextLeft != null || nextRight != null);

            if (!recursive) {
                if (reportTrees) {
                    return Iterators.singletonIterator(new DiffEntry(nextLeft, nextRight));
                } else {
                    return Iterators.emptyIterator();
                }
            }
            RevTree fromTree = resolveSubtree(nextLeft);
            RevTree toTree = resolveSubtree(nextRight);

            Iterator<DiffEntry> it;

            it = new TreeDiffEntryIterator(nextLeft, nextRight, fromTree, toTree, reportTrees,
                    recursive, objectDb);

            return it;
        }

        private @Nullable
        RevTree resolveSubtree(@Nullable NodeRef treeRef) {
            if (treeRef == null) {
                return null;
            }
            ObjectId id = treeRef.objectId();
            RevTree tree = objectDb.getTree(id);
            return tree;
        }
    }

    /**
     * Function that converts a single {@link Node} to an add or remove {@link DiffEntry}
     */
    private static class RefToDiffEntry implements Function<NodeRef, DiffEntry> {

        private ChangeType changeType;

        public RefToDiffEntry(ChangeType changeType) {
            Preconditions.checkArgument(ADDED.equals(changeType) || REMOVED.equals(changeType));
            this.changeType = changeType;
        }

        @Override
        public DiffEntry apply(final NodeRef ref) {
            if (ADDED.equals(changeType)) {
                return new DiffEntry(null, ref);
            }
            return new DiffEntry(ref, null);
        }

    }

    private class BucketBucketDiff extends AbstractIterator<DiffEntry> {

        /**
         * A multi-map of bucket/objectId where key is guaranteed to have two entries, the first one
         * for the left tree id and the second one for he right tree id.
         */
        private final ListMultimap<Integer, Optional<Bucket>> leftRightBuckets;

        private final Iterator<Integer> combinedBuckets;

        private Iterator<DiffEntry> currentBucketIterator;

        private NodeRef leftRef;

        private NodeRef rightRef;

        public BucketBucketDiff(final NodeRef leftRef, final NodeRef rightRef,
                final ImmutableSortedMap<Integer, Bucket> left,
                final ImmutableSortedMap<Integer, Bucket> right) {

            this.leftRef = leftRef;
            this.rightRef = rightRef;
            int expectedKeys = left.size() + right.size();
            int expectedValuesPerKey = 2;
            leftRightBuckets = ArrayListMultimap.create(expectedKeys, expectedValuesPerKey);

            Set<Integer> buckets = Sets.newTreeSet(Sets.union(left.keySet(), right.keySet()));
            for (Integer bucket : buckets) {
                leftRightBuckets.put(bucket, Optional.fromNullable(left.get(bucket)));
                leftRightBuckets.put(bucket, Optional.fromNullable(right.get(bucket)));
            }
            this.combinedBuckets = leftRightBuckets.keySet().iterator();
        }

        @Override
        protected DiffEntry computeNext() {
            if (currentBucketIterator != null && currentBucketIterator.hasNext()) {
                return currentBucketIterator.next();
            }
            if (!combinedBuckets.hasNext()) {
                return endOfData();
            }

            while (combinedBuckets.hasNext()) {
                final Integer bucket = combinedBuckets.next();
                final Optional<Bucket> leftBucket = leftRightBuckets.get(bucket).get(0);
                final Optional<Bucket> rightBucket = leftRightBuckets.get(bucket).get(1);

                if (Objects.equal(leftBucket, rightBucket)) {
                    continue;
                }

                final RevTree left = resolveTree(leftBucket);
                final RevTree right = resolveTree(rightBucket);

                this.currentBucketIterator = new TreeDiffEntryIterator(leftRef, rightRef, left,
                        right, reportTrees, recursive, objectDb);
                break;
            }
            return computeNext();
        }

        private RevTree resolveTree(Optional<Bucket> bucket) {
            RevTree bucketTree = RevTree.EMPTY;
            if (bucket.isPresent()) {
                bucketTree = objectDb.getTree(bucket.get().id());
            }
            return bucketTree;
        }
    }
}