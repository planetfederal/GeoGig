/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.osm.internal.log;

import org.locationtech.geogig.api.ObjectId;

import com.google.common.base.Preconditions;

public class OSMMappingLogEntry {

    private ObjectId postMappingId;

    private ObjectId preMappingId;

    public OSMMappingLogEntry(ObjectId preMappingId, ObjectId postMappingId) {
        this.preMappingId = preMappingId;
        this.postMappingId = postMappingId;
    }

    public ObjectId getPostMappingId() {
        return postMappingId;
    }

    public ObjectId getPreMappingId() {
        return preMappingId;
    }

    public String toString() {
        return preMappingId.toString() + "\t" + postMappingId.toString();
    }

    public static OSMMappingLogEntry fromString(String s) {
        String[] tokens = s.split("\t");
        Preconditions.checkArgument(tokens.length == 2, "Wrong mapping log entry definition");
        return new OSMMappingLogEntry(ObjectId.valueOf(tokens[0]), ObjectId.valueOf(tokens[1]));
    }
}
