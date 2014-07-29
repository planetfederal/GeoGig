/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing;

import java.io.File;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.GeoGIG;
import org.locationtech.geogig.api.MemoryModule;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.TestPlatform;
import org.locationtech.geogig.api.plumbing.diff.DiffEntry;
import org.locationtech.geogig.di.GeogigModule;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

/**
 *
 */
public class DiffTreeTest extends Assert {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private DiffTree command;

    private GeoGIG fakeGeogig;

    @Before
    public void setUp() {

        File workingDirectory = tempFolder.newFolder("mockWorkingDir");
        Platform testPlatform = new TestPlatform(workingDirectory);
        Context injector = Guice.createInjector(Modules.override(new GeogigModule()).with(
                new MemoryModule(testPlatform))).getInstance(org.locationtech.geogig.api.Context.class);

        fakeGeogig = new GeoGIG(injector);
        assertNotNull(fakeGeogig.getOrCreateRepository());
        command = fakeGeogig.command(DiffTree.class);
    }

    @Test
    public void testNoOldVersionSet() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("old version");
        command.call();
    }

    @Test
    public void testNoNewVersionSet() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("new version");
        command.setOldVersion(Ref.HEAD).call();
    }

    @Test
    public void testInvalidOldVersion() {
        exception.expect(IllegalArgumentException.class);
        command.setOldVersion("abcdef0123").setNewVersion(Ref.HEAD).call();
    }

    @Test
    public void testInvalidNewVersion() {
        exception.expect(IllegalArgumentException.class);
        command.setOldVersion(Ref.HEAD).setNewVersion("abcdef0123").call();
    }

    @Test
    public void testNullTrees() {
        Iterator<DiffEntry> diffs = command.setOldTree(ObjectId.NULL).setNewTree(ObjectId.NULL)
                .call();
        assertFalse(diffs.hasNext());
    }

    @Test
    public void testNoCommitsYet() {
        assertFalse(command.setOldVersion(Ref.HEAD).setNewVersion(Ref.HEAD).call().hasNext());
    }
}
