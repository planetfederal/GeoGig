/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage;

import org.locationtech.geogig.api.RevObject;

/**
 * Encapsulates a transaction.
 * <p>
 * Use the same ObjectInserter for a single transaction
 * </p>
 * 
 */
public class ObjectInserter {

    private ObjectDatabase objectDb;

    // TODO: transaction management
    /**
     * Constructs a new {@code ObjectInserter} with the given {@link ObjectDatabase}.
     * 
     * @param objectDatabase the database to insert to
     */
    public ObjectInserter(ObjectDatabase objectDatabase) {
        objectDb = objectDatabase;
    }

    /**
     * @param object
     */
    public void insert(RevObject object) {
        objectDb.put(object);
    }

}
