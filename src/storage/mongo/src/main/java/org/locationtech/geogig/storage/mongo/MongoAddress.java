/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.mongo;

import com.google.common.base.Objects;

/**
 * A value object containing connection info for Mongo databases.
 * These are used as keys for the connection managers.
 * 
 * @see MongoObjectDatabase
 * @see MongoGraphDatabase
 * @see MongoStagingDatabase
 */
final class MongoAddress {
    private final String uri;

    public MongoAddress(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(MongoAddress.class).addValue(uri).toString();
    }
}
