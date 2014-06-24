/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.bdbje;

import java.io.File;

import org.geogit.api.TestPlatform;
import org.geogit.repository.Hints;
import org.geogit.storage.ConfigDatabase;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.GraphDatabaseStressTest;
import org.geogit.storage.fs.IniFileConfigDatabase;

import com.google.common.base.Preconditions;

public class JEGraphDatabaseV2StressTest extends GraphDatabaseStressTest {
    // instance variable so its reused as if it were the singleton in the guice config
    private EnvironmentBuilder envProvider;

    @Override
    protected GraphDatabase createDatabase(TestPlatform platform) {
        File root = platform.pwd();
        Preconditions.checkState(new File(root, ".geogit").exists());

        envProvider = new EnvironmentBuilder(platform);

        ConfigDatabase configDB = new IniFileConfigDatabase(platform);
        return new JEGraphDatabase_v0_1(configDB, envProvider, new Hints());
    }

}
