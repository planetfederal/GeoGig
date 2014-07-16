/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.performance.je;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.di.GeogitModule;
import org.locationtech.geogig.storage.bdbje.JEStorageModule;
import org.locationtech.geogig.test.performance.RevTreeBuilderPerformanceTest;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

public class JERevTreeBuilderPerformanceTest extends RevTreeBuilderPerformanceTest {
    @Override
    protected Context createInjector() {
        return Guice.createInjector(
                Modules.override(new GeogitModule()).with(new JEStorageModule())).getInstance(
                Context.class);
    }
}
