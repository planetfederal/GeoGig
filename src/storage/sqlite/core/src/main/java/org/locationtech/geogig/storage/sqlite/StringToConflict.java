/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.sqlite;

import org.locationtech.geogig.api.plumbing.merge.Conflict;

import com.google.common.base.Function;

/**
 * Function to convert string to conflict.
 * 
 * @author Justin Deoliveira, Boundless
 * 
 */
public class StringToConflict implements Function<String, Conflict> {

    public static final StringToConflict INSTANCE = new StringToConflict();

    @Override
    public Conflict apply(String str) {
        return Conflict.valueOf(str);
    }

}
