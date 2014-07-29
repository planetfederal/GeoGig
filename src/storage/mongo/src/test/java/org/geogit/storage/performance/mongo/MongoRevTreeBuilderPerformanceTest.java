/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.performance.mongo;

import org.geogit.api.Context;
import org.geogit.di.GeogitModule;
import org.geogit.storage.integration.mongo.MongoTestStorageModule;
import org.geogit.test.performance.RevTreeBuilderPerformanceTest;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

public class MongoRevTreeBuilderPerformanceTest extends RevTreeBuilderPerformanceTest {
    @Override
    protected Context createInjector() {
        return Guice.createInjector(
                Modules.override(new GeogitModule()).with(new MongoTestStorageModule()))
                .getInstance(Context.class);
    }
}
