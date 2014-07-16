/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.osm.internal;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.locationtech.geogig.api.porcelain.AddOp;
import org.locationtech.geogig.api.porcelain.CommitOp;
import org.locationtech.geogig.test.integration.RepositoryTestCase;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;

import com.google.common.collect.Lists;

public class CreateOSMChangesetOpTest extends RepositoryTestCase {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        repo.configDatabase().put("user.name", "groldan");
        repo.configDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testCreateChangesets() throws Exception {
        String filename = getClass().getResource("nodes_for_changeset.xml").getFile();
        File file = new File(filename);
        geogit.command(OSMImportOp.class).setDataSource(file.getAbsolutePath()).call();
        long unstaged = geogit.getRepository().workingTree().countUnstaged("node").count();
        assertTrue(unstaged > 0);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).setMessage("commit1").call();
        filename = getClass().getResource("nodes_for_changeset2.xml").getFile();
        file = new File(filename);
        geogit.command(OSMImportOp.class).setDataSource(file.getAbsolutePath()).call();
        unstaged = geogit.getRepository().workingTree().countUnstaged("node").count();
        assertTrue(unstaged > 0);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).setMessage("commit2").call();
        Iterator<ChangeContainer> changes = geogit.command(CreateOSMChangesetOp.class)
                .setNewVersion("HEAD").setOldVersion("HEAD~1").call();
        List<ChangeContainer> list = Lists.newArrayList(changes);
        assertEquals(3, list.size());
    }

    @Test
    public void testCreateChangesetsWithIdReplacement() throws Exception {
        String filename = getClass().getResource("nodes_for_changeset.xml").getFile();
        File file = new File(filename);
        geogit.command(OSMImportOp.class).setDataSource(file.getAbsolutePath()).call();
        long unstaged = geogit.getRepository().workingTree().countUnstaged("node").count();
        assertTrue(unstaged > 0);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).setMessage("commit1").call();
        filename = getClass().getResource("nodes_for_changeset3.xml").getFile();
        file = new File(filename);
        geogit.command(OSMImportOp.class).setDataSource(file.getAbsolutePath()).call();
        unstaged = geogit.getRepository().workingTree().countUnstaged("node").count();
        assertTrue(unstaged > 0);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).setMessage("commit2").call();
        Iterator<ChangeContainer> changes = geogit.command(CreateOSMChangesetOp.class)
                .setNewVersion("HEAD").setOldVersion("HEAD~1").setId(1l).call();
        List<ChangeContainer> list = Lists.newArrayList(changes);
        assertEquals(3, list.size());
        assertEquals(1l, list.get(0).getEntityContainer().getEntity().getChangesetId());
    }

}
