/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.di.caching;

import org.locationtech.geogig.di.Decorator;
import org.locationtech.geogig.di.GeogigModule;
import org.locationtech.geogig.storage.ConfigDatabase;
import org.locationtech.geogig.storage.ObjectDatabase;
import org.locationtech.geogig.storage.StagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * 
 * <p>
 * Depends on {@link GeogigModule} or similar that provides bindings for {@link ConfigDatabase},
 * {@link ObjectDatabase}, and {@link StagingDatabase}.
 * 
 * @see CacheFactory
 * @see ObjectDatabaseCacheInterceptor
 * @see ObjectDatabaseDeleteCacheInterceptor
 * @see ObjectDatabaseDeleteAllCacheInterceptor
 */

public class CachingModule extends AbstractModule {

    /**
     */
    @Override
    protected void configure() {
        // bind separate caches for the object and staging databases

        bind(ObjectDatabaseCacheFactory.class).in(Scopes.SINGLETON);
        bind(StagingDatabaseCacheFactory.class).in(Scopes.SINGLETON);

        Decorator objectCachingDecorator = ObjectDatabaseCacheInterceptor
                .objects(getProvider(ObjectDatabaseCacheFactory.class));

        Decorator indexCachingDecorator = ObjectDatabaseCacheInterceptor
                .staging(getProvider(StagingDatabaseCacheFactory.class));

        GeogigModule.bindDecorator(binder(), objectCachingDecorator);
        GeogigModule.bindDecorator(binder(), indexCachingDecorator);
    }
}
