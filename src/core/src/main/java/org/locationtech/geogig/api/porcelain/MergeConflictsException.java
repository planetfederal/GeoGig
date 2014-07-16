/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

import org.locationtech.geogig.api.ObjectId;

/**
 * Exception that indicates that a merge operation cannot be finished due to merge conflicts
 */
public class MergeConflictsException extends ConflictsException {

    private static final long serialVersionUID = 1L;

    private ObjectId ours = null;

    private ObjectId theirs = null;

    public MergeConflictsException(String msg) {
        super(msg);
    }

    public MergeConflictsException(String msg, ObjectId ours, ObjectId theirs) {
        super(msg);
        this.ours = ours;
        this.theirs = theirs;
    }

    public ObjectId getOurs() {
        return this.ours;
    }

    public ObjectId getTheirs() {
        return this.theirs;
    }

}
