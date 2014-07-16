/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.integration.mongo;

import org.locationtech.geogig.test.integration.OnlineTestProperties;

public class IniMongoProperties extends OnlineTestProperties {

    public IniMongoProperties() {
        super(".geogig-mongo-tests.properties", "mongodb.uri", "mongodb://localhost:27017/",
                "mongodb.database", "geogig");
    }
}
