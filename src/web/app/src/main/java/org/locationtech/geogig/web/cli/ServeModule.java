/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.web.cli;

import org.locationtech.geogig.cli.CLIModule;
import org.locationtech.geogig.web.cli.commands.Serve;

import com.google.inject.Binder;

public class ServeModule implements CLIModule {

    @Override
    public void configure(Binder binder) {
        binder.bind(Serve.class);
    }

}
