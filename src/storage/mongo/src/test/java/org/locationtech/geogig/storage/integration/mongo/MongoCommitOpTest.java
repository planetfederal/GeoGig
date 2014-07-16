/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.integration.mongo;

import java.io.File;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.api.TestPlatform;
import org.locationtech.geogig.di.GeogitModule;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.util.Modules;

public class MongoCommitOpTest extends org.locationtech.geogig.test.integration.CommitOpTest {
    @Rule
    public TemporaryFolder mockWorkingDirTempFolder = new TemporaryFolder();

    @Override
    protected Context createInjector() {
        File workingDirectory;
        try {
            workingDirectory = mockWorkingDirTempFolder.getRoot();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        Platform testPlatform = new TestPlatform(workingDirectory);
        return Guice.createInjector(
                Modules.override(new GeogitModule()).with(new MongoTestStorageModule(),
                        new TestModule(testPlatform))).getInstance(Context.class);
    }

}
