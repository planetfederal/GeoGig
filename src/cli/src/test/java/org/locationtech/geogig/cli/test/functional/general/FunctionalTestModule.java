/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.cli.test.functional.general;

import org.locationtech.geogig.api.Platform;

import com.google.inject.AbstractModule;

/**
 * Guice module with tweaks to run functional tests on the target {@link Platform}'s working
 * directory.
 * 
 * @see CLITestContextBuilder
 */
public class FunctionalTestModule extends AbstractModule {

    private Platform testPlatform;

    /**
     * @param testPlatform
     */
    public FunctionalTestModule(Platform testPlatform) {
        this.testPlatform = testPlatform;
    }

    @Override
    protected void configure() {
        if (testPlatform != null) {
            bind(Platform.class).toInstance(testPlatform);
        }
    }

}
