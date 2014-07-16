/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.geotools.cli.test.functional;

import org.locationtech.geogig.test.integration.OnlineTestProperties;

public class IniOracleProperties extends OnlineTestProperties {

    private static final String[] DEFAULTS = {//
    "database.host", "192.168.1.99",//
            "database.port", "1521",//
            "database.schema", "ORACLE",//
            "database.database", "ORCL",//
            "database.user", "oracle",//
            "database.password", "oracle"//
    };

    public IniOracleProperties() {
        super(".geogit-oracle-tests.properties", DEFAULTS);
    }
}
