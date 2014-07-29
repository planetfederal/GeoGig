/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.performance.je;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.di.GeogigModule;
import org.locationtech.geogig.test.integration.je.JETestStorageModule;
import org.locationtech.geogig.test.performance.LogOpPerformanceTest;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

public class JELogOpPerformanceTest extends LogOpPerformanceTest {
    @Override
    protected Context createInjector() {
        return Guice.createInjector(
                Modules.override(new GeogigModule()).with(new JETestStorageModule())).getInstance(
                Context.class);
    }
}
