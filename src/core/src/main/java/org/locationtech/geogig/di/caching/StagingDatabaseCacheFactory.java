/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.di.caching;

import org.locationtech.geogig.storage.ConfigDatabase;

import com.google.inject.Inject;
import com.google.inject.Provider;

class StagingDatabaseCacheFactory extends CacheFactory {

    @Inject
    public StagingDatabaseCacheFactory(Provider<ConfigDatabase> configDb) {
        super("stagingdb.cache", configDb);
    }

}
