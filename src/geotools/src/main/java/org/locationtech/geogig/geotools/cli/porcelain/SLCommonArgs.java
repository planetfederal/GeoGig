/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.cli.porcelain;

import com.beust.jcommander.Parameter;

/**
 * Common arguments for SpatiaLite porcelain commands.
 */
public class SLCommonArgs {

    /**
     * The database to connect to. Default: database
     */
    @Parameter(names = "--database", description = "The database to connect to.  Default: database.sqlite")
    public String database = "database.sqlite";

    /**
     * User name. Default: user
     */
    @Parameter(names = "--user", description = "User name.  Default: user")
    public String username = "user";

}
