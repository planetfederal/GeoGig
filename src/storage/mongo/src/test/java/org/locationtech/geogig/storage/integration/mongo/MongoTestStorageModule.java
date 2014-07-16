/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.storage.integration.mongo;

import org.locationtech.geogig.storage.ConfigDatabase;
import org.locationtech.geogig.storage.GraphDatabase;
import org.locationtech.geogig.storage.ObjectDatabase;
import org.locationtech.geogig.storage.StagingDatabase;
import org.locationtech.geogig.storage.mongo.MongoConnectionManager;
import org.locationtech.geogig.storage.mongo.MongoGraphDatabase;
import org.locationtech.geogig.storage.mongo.MongoObjectDatabase;
import org.locationtech.geogig.storage.mongo.MongoStagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class MongoTestStorageModule extends AbstractModule {
    @Override
    protected void configure() {
        // Mongo bindings for the different kinds of databases
        bind(MongoConnectionManager.class).in(Scopes.SINGLETON);
        bind(ObjectDatabase.class).to(MongoObjectDatabase.class).in(
                Scopes.SINGLETON);
        bind(StagingDatabase.class).to(MongoStagingDatabase.class).in(
                Scopes.SINGLETON);
        bind(GraphDatabase.class).to(MongoGraphDatabase.class).in(Scopes.SINGLETON);
        bind(ConfigDatabase.class).to(TestConfigDatabase.class);
    }
}
