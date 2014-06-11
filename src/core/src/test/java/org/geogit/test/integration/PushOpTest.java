/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.porcelain.BranchCreateOp;
import org.geogit.api.porcelain.CheckoutOp;
import org.geogit.api.porcelain.CloneOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.LogOp;
import org.geogit.api.porcelain.PushOp;
import org.geogit.api.porcelain.SynchronizationException;
import org.geogit.remote.RemoteRepositoryTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Optional;

public class PushOpTest extends RemoteRepositoryTestCase {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private LinkedList<RevCommit> expectedMaster;

    private LinkedList<RevCommit> expectedBranch;

    @Override
    protected void setUpInternal() throws Exception {
        // Commit several features to the remote

        expectedMaster = new LinkedList<RevCommit>();
        expectedBranch = new LinkedList<RevCommit>();

        insertAndAdd(remoteGeogit.geogit, points1);
        RevCommit commit = remoteGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);
        expectedBranch.addFirst(commit);

        // Create and checkout branch1
        remoteGeogit.geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("Branch1")
                .call();

        // Commit some changes to branch1
        insertAndAdd(remoteGeogit.geogit, points2);
        commit = remoteGeogit.geogit.command(CommitOp.class).call();
        expectedBranch.addFirst(commit);

        insertAndAdd(remoteGeogit.geogit, points3);
        commit = remoteGeogit.geogit.command(CommitOp.class).call();
        expectedBranch.addFirst(commit);

        // Make sure Branch1 has all of the commits
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedBranch, logged);

        // Checkout master and commit some changes
        remoteGeogit.geogit.command(CheckoutOp.class).setSource("master").call();

        insertAndAdd(remoteGeogit.geogit, lines1);
        commit = remoteGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);

        insertAndAdd(remoteGeogit.geogit, lines2);
        commit = remoteGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);

        // Make sure master has all of the commits
        logs = remoteGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMaster, logged);

        // Make sure the local repository has no commits prior to clone
        logs = localGeogit.geogit.command(LogOp.class).call();
        assertNotNull(logs);
        assertFalse(logs.hasNext());

        // clone from the remote
        CloneOp clone = clone();
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).setBranch("Branch1").call();

        // Make sure the local repository got all of the commits
        logs = localGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedBranch, logged);

        // Make sure the local master matches the remote
        localGeogit.geogit.command(CheckoutOp.class).setSource("master").call();

        logs = localGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMaster, logged);
    }

    @Test
    public void testPush() throws Exception {
        // Add a commit to the local repository
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);

        // Push the commit
        PushOp push = push();
        push.call();

        // verify that the remote got the commit
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMaster, logged);

        // verify that the local reference of the remote master is updated
        Optional<Ref> ref = localGeogit.geogit.command(RefParse.class)
                .setName(Ref.append(Ref.REMOTES_PREFIX, "origin/master")).call();
        assertTrue(ref.isPresent());
        assertEquals(logged.get(0).getId(), ref.get().getObjectId());
    }

    @Test
    public void testPushToRemote() throws Exception {
        // Add a commit to the local repository
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);

        // Push the commit
        PushOp push = push();
        push.setRemote("origin").call();

        // verify that the remote got the commit
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMaster, logged);

        // verify that the local reference of the remote master is updated
        Optional<Ref> ref = localGeogit.geogit.command(RefParse.class)
                .setName(Ref.append(Ref.REMOTES_PREFIX, "origin/master")).call();
        assertTrue(ref.isPresent());
        assertEquals(logged.get(0).getId(), ref.get().getObjectId());

    }

    @Test
    public void testPushToRemoteHEAD() throws Exception {
        insertAndAdd(localGeogit.geogit, lines3);
        localGeogit.geogit.command(CommitOp.class).call();

        PushOp push = push();
        try {
            push.setRemote("origin").addRefSpec("HEAD").call();
            fail();
        } catch (SynchronizationException e) {
            assertEquals(SynchronizationException.StatusCode.CANNOT_PUSH_TO_SYMBOLIC_REF,
                    e.statusCode);
        }

    }

    @Test
    public void testPushAll() throws Exception {
        // Add a commit to the local repository
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);

        localGeogit.geogit.command(CheckoutOp.class).setSource("Branch1").call();
        insertAndAdd(localGeogit.geogit, points1_modified);
        RevCommit commit2 = localGeogit.geogit.command(CommitOp.class).call();
        expectedBranch.addFirst(commit2);

        // Push the commit
        PushOp push = push();
        push.setAll(true).call();

        // verify that the remote got the commit on both branches
        remoteGeogit.geogit.command(CheckoutOp.class).setSource("master").call();
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }
        assertEquals(expectedMaster, logged);

        remoteGeogit.geogit.command(CheckoutOp.class).setSource("Branch1").call();
        logs = remoteGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }
        assertEquals(expectedBranch, logged);

    }

    @Test
    public void testPushWithRefSpec() throws Exception {
        // Add a commit to the local repository
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);

        // Push the commit
        PushOp push = push();
        push.addRefSpec("master:NewRemoteBranch");
        push.call();

        assertTrue(remoteGeogit.geogit.command(RefParse.class).setName("NewRemoteBranch").call()
                .isPresent());

        // verify that the remote got the commit
        remoteGeogit.geogit.command(CheckoutOp.class).setSource("NewRemoteBranch").call();
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMaster, logged);

        // verify that the local reference of the remote master is updated
        Optional<Ref> ref = localGeogit.geogit.command(RefParse.class)
                .setName(Ref.append(Ref.REMOTES_PREFIX, "origin/NewRemoteBranch")).call();
        assertTrue(ref.isPresent());
        assertEquals(logged.get(0).getId(), ref.get().getObjectId());
    }

    @Test
    public void testPushWithMultipleRefSpecs() throws Exception {
        // Add a commit to the local repository
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);

        // Push the commit
        PushOp push = push();
        push.addRefSpec("master:NewRemoteBranch");
        push.addRefSpec("Branch1:NewRemoteBranch2");
        push.call();

        assertTrue(remoteGeogit.geogit.command(RefParse.class).setName("NewRemoteBranch").call()
                .isPresent());
        assertTrue(remoteGeogit.geogit.command(RefParse.class).setName("NewRemoteBranch2").call()
                .isPresent());

        // verify that the remote got the commit
        remoteGeogit.geogit.command(CheckoutOp.class).setSource("NewRemoteBranch").call();
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMaster, logged);

        remoteGeogit.geogit.command(CheckoutOp.class).setSource("NewRemoteBranch2").call();
        logs = remoteGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedBranch, logged);
    }

    @Test
    public void testDeleteRemoteBranch() throws Exception {
        PushOp push = push();
        push.addRefSpec(":Branch1");
        push.call();

        assertFalse(remoteGeogit.geogit.command(RefParse.class).setName("Branch1").call()
                .isPresent());
    }

    @Test
    public void testPushWithDefaultRefSpec() throws Exception {
        // Add a commit to the local repository
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedMaster.addFirst(commit);

        // Push the commit
        PushOp push = push();
        push.addRefSpec(":");
        push.call();

        // verify that the remote got the commit
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMaster, logged);
    }

    @Test
    public void testPushBranch() throws Exception {
        // Add a commit to the local repository
        localGeogit.geogit.command(CheckoutOp.class).setSource("Branch1").call();
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedBranch.addFirst(commit);
        localGeogit.geogit.command(CheckoutOp.class).setSource("master").call();

        // Push the commit
        PushOp push = push();
        push.addRefSpec("Branch1");
        push.call();

        // verify that the remote got the commit
        Optional<Ref> remoteRef = remoteGeogit.geogit.command(RefParse.class).setName("Branch1")
                .call();
        assertTrue(remoteRef.isPresent());
        assertTrue(remoteRef.get().getName().startsWith(Ref.HEADS_PREFIX));
        remoteGeogit.geogit.command(CheckoutOp.class).setSource("Branch1").call();
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedBranch, logged);
    }

    @Test
    public void testPushBranchForce() throws Exception {
        // Add a commit to the local repository
        localGeogit.geogit.command(CheckoutOp.class).setSource("Branch1").call();
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedBranch.addFirst(commit);
        localGeogit.geogit.command(CheckoutOp.class).setSource("master").call();

        // Push the commit
        PushOp push = push();
        push.addRefSpec("+Branch1");
        push.call();

        // verify that the remote got the commit
        remoteGeogit.geogit.command(CheckoutOp.class).setSource("Branch1").call();
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedBranch, logged);
    }

    @Test
    public void testPushTooManyRefArgs() throws Exception {
        // Add a commit to the local repository
        insertAndAdd(localGeogit.geogit, lines3);
        RevCommit commit = localGeogit.geogit.command(CommitOp.class).call();
        expectedBranch.addFirst(commit);

        // Push the commit
        PushOp push = push();
        push.addRefSpec("Branch1:master:HEAD");
        exception.expect(IllegalArgumentException.class);
        push.call();
    }
}
