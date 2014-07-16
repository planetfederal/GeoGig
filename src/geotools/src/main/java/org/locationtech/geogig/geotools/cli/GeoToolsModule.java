/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.cli;

import org.locationtech.geogig.cli.CLIModule;

import com.google.inject.Binder;

/**
 * Provides bindings for GeoTools command extensions to the GeoGit command line interface.
 * 
 * @see PGCommandProxy
 * @see ShpCommandProxy
 * @see SLCommandProxy
 * @see OracleCommandProxy
 * @see SQLServerCommandProxy
 * @see GeoJsonCommandProxy
 */

public class GeoToolsModule implements CLIModule {

    /**
     * @see CLIModule#configure(com.google.inject.Binder)
     */
    @Override
    public void configure(Binder binder) {
        binder.bind(PGCommandProxy.class);
        binder.bind(ShpCommandProxy.class);
        binder.bind(SLCommandProxy.class);
        binder.bind(OracleCommandProxy.class);
        binder.bind(SQLServerCommandProxy.class);
        binder.bind(GeoJsonCommandProxy.class);
    }

}
