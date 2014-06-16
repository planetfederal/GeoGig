/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.geogit.web.cli;

import org.geogit.cli.CLIModule;
import org.geogit.web.cli.commands.Serve;

import com.google.inject.Binder;

public class ServeModule implements CLIModule {

    @Override
    public void configure(Binder binder) {
        binder.bind(Serve.class);
    }

}
