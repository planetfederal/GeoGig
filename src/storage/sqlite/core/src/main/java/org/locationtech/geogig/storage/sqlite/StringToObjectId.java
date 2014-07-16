/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.sqlite;

import org.locationtech.geogig.api.ObjectId;

import com.google.common.base.Function;

/**
 * Function to convert string to object id.
 * 
 * @author Justin Deoliveira, Boundless
 * 
 */
public class StringToObjectId implements Function<String, ObjectId> {

    public static StringToObjectId INSTANCE = new StringToObjectId();

    @Override
    public ObjectId apply(String str) {
        return ObjectId.valueOf(str);
    }

}
