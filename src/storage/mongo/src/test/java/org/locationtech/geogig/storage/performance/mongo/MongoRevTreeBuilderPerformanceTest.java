/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.performance.mongo;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.di.GeogigModule;
import org.locationtech.geogig.storage.integration.mongo.MongoTestStorageModule;
import org.locationtech.geogig.test.performance.RevTreeBuilderPerformanceTest;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

public class MongoRevTreeBuilderPerformanceTest extends RevTreeBuilderPerformanceTest {
    @Override
    protected Context createInjector() {
        return Guice.createInjector(
                Modules.override(new GeogigModule()).with(new MongoTestStorageModule()))
                .getInstance(Context.class);
    }
}
