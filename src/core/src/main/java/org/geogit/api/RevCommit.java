/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public interface RevCommit extends RevObject {

    /**
     * @return the id of the tree this commit points to
     */
    public ObjectId getTreeId();

    /**
     * @return the parentIds
     */
    public ImmutableList<ObjectId> getParentIds();

    /**
     * Short cut for {@code getParentIds().get(parentIndex)}.
     * <p>
     * Beware {@code parentIndex} is <b>zero-based</b>, whilst the command line interface syntax for
     * parents is one-based (e.g. {@code <commit id>^1} for the first parent instead of
     * {@code <commit id>^0}).
     * 
     * @param parentIndex
     * @return the parent id at the given index, or absent
     */
    public Optional<ObjectId> parentN(int parentIndex);

    /**
     * @return the author
     */
    public RevPerson getAuthor();

    /**
     * @return the committer
     */
    public RevPerson getCommitter();

    /**
     * @return the message
     */
    public String getMessage();

}