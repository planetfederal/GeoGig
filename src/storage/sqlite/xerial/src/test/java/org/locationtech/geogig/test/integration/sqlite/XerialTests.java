/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration.sqlite;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.api.TestPlatform;
import org.locationtech.geogig.di.GeogitModule;
import org.locationtech.geogig.storage.sqlite.Xerial;
import org.locationtech.geogig.storage.sqlite.XerialSQLiteModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;

/**
 * Test utility class.
 * 
 * @author Justin Deoliveira, Boundless
 * 
 */
public class XerialTests {

    /**
     * Creates the injector to enable xerial sqlite storage.
     */
    public static Context injector(final TestPlatform platform) {
        return Guice.createInjector(Modules.override(new GeogitModule()).with(
                new XerialSQLiteModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        Xerial.turnSynchronizationOff();
                        bind(Platform.class).toInstance(platform);
                    }
                })).getInstance(Context.class);
    }
}
