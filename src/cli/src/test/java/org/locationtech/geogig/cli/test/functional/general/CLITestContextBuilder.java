/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.cli.test.functional.general;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.ContextBuilder;
import org.locationtech.geogig.api.TestPlatform;
import org.locationtech.geogig.cli.CLIContextBuilder;
import org.locationtech.geogig.di.GeogitModule;
import org.locationtech.geogig.di.PluginsModule;
import org.locationtech.geogig.di.caching.CachingModule;
import org.locationtech.geogig.repository.Hints;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

public class CLITestContextBuilder extends ContextBuilder {

    private TestPlatform platform;

    public CLITestContextBuilder(TestPlatform platform) {
        this.platform = platform;
    }

    @Override
    public Context build(Hints hints) {
        FunctionalTestModule functionalTestModule = new FunctionalTestModule(platform.clone());

        Context context = Guice.createInjector(
                Modules.override(new GeogitModule()).with(new PluginsModule(),
                        new CLIContextBuilder.DefaultPlugins(), functionalTestModule,
                        new HintsModule(hints), new CachingModule())).getInstance(Context.class);
        return context;
    }

}
