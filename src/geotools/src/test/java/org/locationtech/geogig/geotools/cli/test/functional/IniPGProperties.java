/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.geotools.cli.test.functional;

import org.locationtech.geogig.test.integration.OnlineTestProperties;

public class IniPGProperties extends OnlineTestProperties {

    private static final String[] DEFAULTS = {//
    "database.host", "localhost",//
            "database.port", "5432",//
            "database.schema", "public",//
            "database.database", "database",//
            "database.user", "postgres",//
            "database.password", "postgres"//
    };

    public IniPGProperties() {
        super(".geogit-pg-tests.properties", DEFAULTS);
    }
}
