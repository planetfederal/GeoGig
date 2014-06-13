/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.repository;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.geogit.api.Node;
import org.geogit.api.ObjectId;
import org.geogit.api.ProgressListener;
import org.geogit.api.RevTree;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.plumbing.diff.DiffObjectCount;
import org.geogit.api.plumbing.merge.Conflict;
import org.geogit.storage.StagingDatabase;

import com.google.common.base.Optional;

/**
 * Serves as an interface for the index of the GeoGit repository.
 * 
 * @see StagingDatabase
 */
public interface StagingArea {

    /**
     * @return the staging database.
     */
    public StagingDatabase getDatabase();

    /**
     * Updates the STAGE_HEAD ref to the specified tree.
     * 
     * @param newTree the tree to set as the new STAGE_HEAD
     */
    public void updateStageHead(ObjectId newTree);

    /**
     * @return the tree represented by STAGE_HEAD. If there is no tree set at STAGE_HEAD, it will
     *         return the HEAD tree (no staged changes).
     */
    public RevTree getTree();

    /**
     * @return true if there are no uncommitted features in the index. False otherwise
     */
    public boolean isClean();

    /**
     * @param path
     * @return the Node for the feature at the specified path if it exists in the index, otherwise
     *         Optional.absent()
     */
    public abstract Optional<Node> findStaged(final String path);

    /**
     * Stages the changes indicated by the {@link DiffEntry} iterator.
     * 
     * @param progress
     * @param unstaged
     * @param numChanges
     */
    public abstract void stage(final ProgressListener progress, final Iterator<DiffEntry> unstaged,
            final long numChanges);

    /**
     * @param pathFilter
     * @return an iterator for all of the differences between STAGE_HEAD and HEAD based on the path
     *         filter.
     */
    public abstract Iterator<DiffEntry> getStaged(final @Nullable List<String> pathFilters);

    /**
     * @param pathFilter
     * @return the number differences between STAGE_HEAD and HEAD based on the path filter.
     */
    public abstract DiffObjectCount countStaged(final @Nullable List<String> pathFilters);

    /**
     * returns the number of conflicted objects in the index, for the given path filter
     * 
     * @param pathFilter
     * @return
     */
    public int countConflicted(final @Nullable String pathFilter);

    /**
     * returns the list of conflicted objects in the index, for the given path filter
     * 
     * @param pathFilter
     * @return
     */
    public List<Conflict> getConflicted(final @Nullable String pathFilter);

}