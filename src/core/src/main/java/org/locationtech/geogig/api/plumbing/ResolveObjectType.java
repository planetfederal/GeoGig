/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing;

import org.locationtech.geogig.api.AbstractGeoGigOp;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.RevObject;
import org.locationtech.geogig.api.RevObject.TYPE;

/**
 * Gets the object type of the object that matches the given {@link ObjectId}.
 */
public class ResolveObjectType extends AbstractGeoGigOp<RevObject.TYPE> {

    private ObjectId oid;

    /**
     * @param oid the {@link ObjectId object id} of the object to check
     * @return {@code this}
     */
    public ResolveObjectType setObjectId(ObjectId oid) {
        this.oid = oid;
        return this;
    }

    /**
     * Executes the command.
     * 
     * @return the type of the object specified by the object id.
     * @throws IllegalArgumentException if the object doesn't exist
     */
    @Override
    protected TYPE _call() throws IllegalArgumentException {
        RevObject o = stagingDatabase().get(oid);
        return o.getType();
    }
}
