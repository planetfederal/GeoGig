/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.ContextBuilder;
import org.locationtech.geogig.api.MemoryModule;
import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.di.GeogitModule;
import org.locationtech.geogig.repository.Hints;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

public class TestContextBuilder extends ContextBuilder {

    Platform platform;

    public TestContextBuilder(Platform platform) {
        this.platform = platform;
    }

    @Override
    public Context build(Hints hints) {
        return Guice.createInjector(
                Modules.override(new GeogitModule()).with(new MemoryModule(platform),
                        new HintsModule(hints))).getInstance(Context.class);
    }

}
