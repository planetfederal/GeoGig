/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.RevCommit;
import org.locationtech.geogig.api.porcelain.BranchCreateOp;
import org.locationtech.geogig.api.porcelain.CommitOp;
import org.locationtech.geogig.test.integration.RepositoryTestCase;

import com.google.common.base.Optional;

public class UpdateRefTest extends RepositoryTestCase {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        injector.configDatabase().put("user.name", "groldan");
        injector.configDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Test
    public void testConstructorAndMutators() throws Exception {
        insertAndAdd(points1);
        RevCommit commit1 = geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("branch1").call();

        insertAndAdd(points2);
        RevCommit commit2 = geogit.command(CommitOp.class).call();
        Optional<Ref> newBranch = geogit.command(UpdateRef.class).setName("refs/heads/branch1")
                .setNewValue(commit2.getId()).setOldValue(commit1.getId()).setReason("Testing")
                .call();

        assertTrue(newBranch.get().getObjectId().equals(commit2.getId()));
        assertFalse(newBranch.get().getObjectId().equals(commit1.getId()));
    }

    @Test
    public void testNoName() {
        exception.expect(IllegalStateException.class);
        geogit.command(UpdateRef.class).call();
    }

    @Test
    public void testNoValue() {
        exception.expect(IllegalStateException.class);
        geogit.command(UpdateRef.class).setName(Ref.MASTER).call();
    }

    @Test
    public void testDeleteRefThatWasASymRef() throws Exception {
        insertAndAdd(points1);
        RevCommit commit1 = geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("branch1").call();

        insertAndAdd(points2);
        RevCommit commit2 = geogit.command(CommitOp.class).call();

        geogit.command(UpdateSymRef.class).setName("refs/heads/branch1")
                .setOldValue(commit1.getId().toString()).setNewValue(Ref.MASTER)
                .setReason("this is a test").call();

        geogit.command(UpdateRef.class).setName("refs/heads/branch1").setNewValue(commit2.getId())
                .setOldValue(Ref.MASTER).call();

        Optional<Ref> branchId = geogit.command(RefParse.class).setName("refs/heads/branch1")
                .call();

        assertTrue(branchId.get().getObjectId().equals(commit2.getId()));

        geogit.command(UpdateRef.class).setDelete(true).setName("refs/heads/branch1").call();
    }

    @Test
    public void testDeleteWithNonexistentName() {
        Optional<Ref> ref = geogit.command(UpdateRef.class).setDelete(true).setName("NoRef").call();
        assertFalse(ref.isPresent());
    }
}
