/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.locationtech.geogig.api.AbstractGeoGitOp;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.RevTree;
import org.locationtech.geogig.api.plumbing.diff.DiffEntry;
import org.locationtech.geogig.api.plumbing.diff.DiffTreeWalk;
import org.locationtech.geogig.storage.ObjectDatabase;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Compares the content and metadata links of blobs found via two tree objects on the repository's
 * {@link ObjectDatabase}
 */
public class DiffTree extends AbstractGeoGitOp<Iterator<DiffEntry>> implements
        Supplier<Iterator<DiffEntry>> {

    private final List<String> pathFilters = Lists.newLinkedList();

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
    protected  Iterator<DiffEntry> _call() throws IllegalArgumentException {
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

        DiffTreeWalk treeWalk = new DiffTreeWalk(stagingDatabase(), oldTree, newTree);
        treeWalk.setFilter(pathFilters);
        treeWalk.setReportTrees(reportTrees);
        treeWalk.setRecursive(recursive);
        return treeWalk.get();
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
