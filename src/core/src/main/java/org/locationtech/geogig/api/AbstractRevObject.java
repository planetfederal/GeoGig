/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api;

/**
 * Base object type accessed during revision walking.
 * 
 * @see RevCommit
 * @see RevTree
 * @see RevFeature
 * @see RevTag
 */
public abstract class AbstractRevObject implements RevObject {
    private final ObjectId id;

    public AbstractRevObject(final ObjectId id) {
        this.id = id;
    }

    /**
     * Get the name of this object.
     * 
     * @return unique hash of this object.
     */
    public final ObjectId getId() {
        return id;
    }

    /**
     * Equality is based on id
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractRevObject)) {
            return false;
        }
        return id.equals(((AbstractRevObject) o).getId());
    }
}
