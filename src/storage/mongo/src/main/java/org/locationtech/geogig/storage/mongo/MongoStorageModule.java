/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.storage.mongo;

import org.locationtech.geogig.storage.GraphDatabase;
import org.locationtech.geogig.storage.ObjectDatabase;
import org.locationtech.geogig.storage.StagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class MongoStorageModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ObjectDatabase.class).to(MongoObjectDatabase.class).in(Scopes.SINGLETON);
        bind(StagingDatabase.class).to(MongoStagingDatabase.class).in(Scopes.SINGLETON);
        bind(GraphDatabase.class).to(MongoGraphDatabase.class).in(Scopes.SINGLETON);
        bind(MongoConnectionManager.class).in(Scopes.NO_SCOPE);
    }
}
