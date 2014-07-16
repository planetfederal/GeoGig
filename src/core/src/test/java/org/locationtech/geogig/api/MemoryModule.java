/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api;

import org.locationtech.geogig.storage.GraphDatabase;
import org.locationtech.geogig.storage.ObjectDatabase;
import org.locationtech.geogig.storage.RefDatabase;
import org.locationtech.geogig.storage.StagingDatabase;
import org.locationtech.geogig.storage.memory.HeapGraphDatabase;
import org.locationtech.geogig.storage.memory.HeapObjectDatabse;
import org.locationtech.geogig.storage.memory.HeapRefDatabase;
import org.locationtech.geogig.storage.memory.HeapStagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @see HeapObjectDatabse
 * @see HeapStagingDatabase
 * @see HeapRefDatabase
 * @see HeapGraphDatabase
 */
public class MemoryModule extends AbstractModule {

    private Platform testPlatform;

    /**
     * @param testPlatform
     */
    public MemoryModule(Platform testPlatform) {
        this.testPlatform = testPlatform;
    }

    @Override
    protected void configure() {
        if (testPlatform != null) {
            bind(Platform.class).toInstance(testPlatform);
        }
        bind(ObjectDatabase.class).to(HeapObjectDatabse.class).in(Scopes.SINGLETON);
        bind(StagingDatabase.class).to(HeapStagingDatabase.class).in(Scopes.SINGLETON);
        bind(RefDatabase.class).to(HeapRefDatabase.class).in(Scopes.SINGLETON);
        bind(GraphDatabase.class).to(HeapGraphDatabase.class).in(Scopes.SINGLETON);
    }

}
