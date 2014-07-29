/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api;

import java.io.File;

public class TestPlatform extends DefaultPlatform implements Platform, Cloneable {

    private File userHomeDirectory;

    public TestPlatform(final File workingDirectory) {
        super.workingDir = workingDirectory;
        this.userHomeDirectory = new File(workingDirectory, "userhome");
        this.userHomeDirectory.mkdir();
    }

    public TestPlatform(final File workingDirectory, final File userHomeDirectory) {
        super.workingDir = workingDirectory;
        this.userHomeDirectory = userHomeDirectory;
    }

    @Override
    public File pwd() {
        return workingDir;
    }

    @Override
    public File getUserHome() {
        return userHomeDirectory;
    }

    public void setUserHome(File userHomeDirectory) {
        this.userHomeDirectory = userHomeDirectory;
    }

    @Override
    public TestPlatform clone() {
        return new TestPlatform(pwd(), getUserHome());
    }
}
