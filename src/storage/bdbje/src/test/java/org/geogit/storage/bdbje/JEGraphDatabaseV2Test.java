/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.bdbje;

import java.io.File;

import org.geogit.api.Platform;
import org.geogit.repository.Hints;
import org.geogit.storage.ConfigDatabase;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.GraphDatabaseTest;
import org.geogit.storage.fs.IniFileConfigDatabase;

import com.google.common.base.Preconditions;

public class JEGraphDatabaseV2Test extends GraphDatabaseTest {

    // instance variable so its reused as if it were the singleton in the guice config
    private EnvironmentBuilder envProvider;

    @Override
    protected GraphDatabase createDatabase(Platform platform) throws Exception {
        File root = platform.pwd();
        Preconditions.checkState(new File(root, ".geogit").exists());

        envProvider = new EnvironmentBuilder(platform);

        ConfigDatabase configDB = new IniFileConfigDatabase(platform);
        return new JEGraphDatabase_v0_2(configDB, envProvider, new Hints());
    }
}