/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration.sqlite;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.storage.ConfigDatabase;
import org.locationtech.geogig.storage.GraphDatabase;
import org.locationtech.geogig.storage.GraphDatabaseTest;
import org.locationtech.geogig.storage.fs.IniFileConfigDatabase;
import org.locationtech.geogig.storage.sqlite.XerialGraphDatabase;

public class XerialGraphDatabaseTest extends GraphDatabaseTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Override
    protected GraphDatabase createDatabase(Platform platform) throws Exception {
        ConfigDatabase configdb = new IniFileConfigDatabase(platform);
        return new XerialGraphDatabase(configdb, platform);
    }
}
