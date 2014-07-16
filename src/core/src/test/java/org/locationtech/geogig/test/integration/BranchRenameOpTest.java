/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.plumbing.RefParse;
import org.locationtech.geogig.api.porcelain.AddOp;
import org.locationtech.geogig.api.porcelain.BranchCreateOp;
import org.locationtech.geogig.api.porcelain.BranchRenameOp;
import org.locationtech.geogig.api.porcelain.CheckoutOp;
import org.locationtech.geogig.api.porcelain.CommitOp;

import com.google.common.base.Optional;

public class BranchRenameOpTest extends RepositoryTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        injector.configDatabase().put("user.name", "groldan");
        injector.configDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Test
    public void NoBranchNameTest() {
        exception.expect(IllegalStateException.class);
        geogit.command(BranchRenameOp.class).call();
    }

    @Test
    public void SameNameTest() {
        exception.expect(IllegalStateException.class);
        geogit.command(BranchRenameOp.class).setNewName("master").setOldName("master").call();
    }

    @Test
    public void RenamingABranchTest() throws Exception {
        insertAndAdd(points1);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();
        Ref TestBranch = geogit.command(BranchCreateOp.class).setName("TestBranch").call();

        Ref SuperTestBranch = geogit.command(BranchRenameOp.class).setOldName("TestBranch")
                .setNewName("SuperTestBranch").call();

        Optional<Ref> result = geogit.command(RefParse.class).setName("TestBranch").call();

        assertFalse(result.isPresent());

        result = geogit.command(RefParse.class).setName("SuperTestBranch").call();

        assertTrue(result.isPresent());

        assertEquals(TestBranch.getObjectId(), SuperTestBranch.getObjectId());
    }

    @Test
    public void NoOldNameTest() throws Exception {
        insertAndAdd(points1);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();
        Ref TestBranch = geogit.command(BranchCreateOp.class).setName("TestBranch")
                .setAutoCheckout(true).call();

        Ref SuperTestBranch = geogit.command(BranchRenameOp.class).setNewName("SuperTestBranch")
                .call();

        Optional<Ref> result = geogit.command(RefParse.class).setName("TestBranch").call();

        assertFalse(result.isPresent());

        result = geogit.command(RefParse.class).setName("SuperTestBranch").call();

        assertTrue(result.isPresent());

        assertEquals(TestBranch.getObjectId(), SuperTestBranch.getObjectId());
    }

    @Test
    public void ForceRenameTest() throws Exception {
        insertAndAdd(points1);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();
        Ref TestBranch1 = geogit.command(BranchCreateOp.class).setName("TestBranch1").call();

        geogit.command(BranchCreateOp.class).setName("TestBranch2").setAutoCheckout(true).call();
        insertAndAdd(points2);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).setMessage("this should be deleted").call();

        geogit.command(CheckoutOp.class).setSource("TestBranch1").call();

        Ref SuperTestBranch = geogit.command(BranchRenameOp.class).setNewName("TestBranch2")
                .setForce(true).call();

        Optional<Ref> result = geogit.command(RefParse.class).setName("TestBranch1").call();

        assertFalse(result.isPresent());

        result = geogit.command(RefParse.class).setName("TestBranch2").call();

        assertTrue(result.isPresent());

        assertEquals(TestBranch1.getObjectId(), SuperTestBranch.getObjectId());

        exception.expect(IllegalStateException.class);
        geogit.command(BranchRenameOp.class).setNewName("master").call();
    }
}
