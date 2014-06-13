/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geogit.api.GeogitTransaction;
import org.geogit.api.RevCommit;
import org.geogit.api.plumbing.TransactionBegin;
import org.geogit.api.plumbing.TransactionEnd;
import org.geogit.api.plumbing.merge.Conflict;
import org.geogit.api.plumbing.merge.ConflictsReadOp;
import org.geogit.api.porcelain.BranchCreateOp;
import org.geogit.api.porcelain.CheckoutOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.LogOp;
import org.geogit.api.porcelain.MergeOp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Suppliers;

public class GeogitTransactionTest extends RepositoryTestCase {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        injector.configDatabase().put("user.name", "groldan");
        injector.configDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Test
    public void testTransaction() throws Exception {
        LinkedList<RevCommit> expectedMain = new LinkedList<RevCommit>();
        LinkedList<RevCommit> expectedTransaction = new LinkedList<RevCommit>();

        // make a commit
        insertAndAdd(points1);
        RevCommit commit = geogit.command(CommitOp.class).call();
        expectedMain.addFirst(commit);
        expectedTransaction.addFirst(commit);

        // start a transaction
        GeogitTransaction t = geogit.command(TransactionBegin.class).call();

        // perform a commit in the transaction
        insertAndAdd(t, points2);
        commit = t.command(CommitOp.class).call();
        expectedTransaction.addFirst(commit);

        // Verify that the base repository is unchanged
        Iterator<RevCommit> logs = geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMain, logged);

        // Verify that the transaction has the commit
        logs = t.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedTransaction, logged);

        // Commit the transaction
        geogit.command(TransactionEnd.class).setTransaction(t).setRebase(true).call();

        // Verify that the base repository has the changes from the transaction
        logs = geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedTransaction, logged);

    }

    @Test
    public void testSyncTransaction() throws Exception {
        LinkedList<RevCommit> expectedMain = new LinkedList<RevCommit>();
        LinkedList<RevCommit> expectedTransaction = new LinkedList<RevCommit>();

        // make a commit
        insertAndAdd(points1);
        RevCommit firstCommit = geogit.command(CommitOp.class).call();
        expectedMain.addFirst(firstCommit);
        expectedTransaction.addFirst(firstCommit);

        // start a transaction
        GeogitTransaction t = geogit.command(TransactionBegin.class).call();

        // perform a commit in the transaction
        insertAndAdd(t, points2);
        RevCommit transactionCommit = t.command(CommitOp.class).call();
        expectedTransaction.addFirst(transactionCommit);

        // perform a commit on the repo
        insertAndAdd(points3);
        RevCommit repoCommit = geogit.command(CommitOp.class).call();
        expectedMain.addFirst(repoCommit);

        // Verify that the base repository is unchanged
        Iterator<RevCommit> logs = geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMain, logged);

        // Verify that the transaction has the commit
        logs = t.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedTransaction, logged);

        // Commit the transaction
        geogit.command(TransactionEnd.class).setTransaction(t).call();

        // Verify that a merge commit was created
        logs = geogit.command(LogOp.class).call();
        RevCommit lastCommit = logs.next();
        assertFalse(lastCommit.equals(repoCommit));
        assertTrue(lastCommit.getMessage().contains("Merge commit"));
        assertEquals(lastCommit.getParentIds().get(0), transactionCommit.getId());
        assertEquals(lastCommit.getParentIds().get(1), repoCommit.getId());
        assertEquals(logs.next(), repoCommit);
        assertEquals(logs.next(), transactionCommit);
        assertEquals(logs.next(), firstCommit);
        assertFalse(logs.hasNext());

    }

    @Test
    public void testTransactionAuthor() throws Exception {
        LinkedList<RevCommit> expectedMain = new LinkedList<RevCommit>();
        LinkedList<RevCommit> expectedTransaction = new LinkedList<RevCommit>();

        // make a commit
        insertAndAdd(points1);
        RevCommit firstCommit = geogit.command(CommitOp.class).call();
        expectedMain.addFirst(firstCommit);
        expectedTransaction.addFirst(firstCommit);

        // start a transaction
        GeogitTransaction t = geogit.command(TransactionBegin.class).call();

        t.setAuthor("Transaction Author", "transaction@author.com");

        // perform a commit in the transaction
        insertAndAdd(t, points2);
        RevCommit transactionCommit = t.command(CommitOp.class).call();
        expectedTransaction.addFirst(transactionCommit);

        // perform a commit on the repo
        insertAndAdd(points3);
        RevCommit repoCommit = geogit.command(CommitOp.class).call();
        expectedMain.addFirst(repoCommit);

        // Verify that the base repository is unchanged
        Iterator<RevCommit> logs = geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMain, logged);

        // Verify that the transaction has the commit
        logs = t.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedTransaction, logged);

        // Commit the transaction
        t.commitSyncTransaction();

        // Verify that a merge commit was created
        logs = geogit.command(LogOp.class).call();
        RevCommit lastCommit = logs.next();
        assertFalse(lastCommit.equals(repoCommit));
        assertTrue(lastCommit.getMessage().contains("Merge commit"));
        assertEquals(lastCommit.getParentIds().get(0), transactionCommit.getId());
        assertEquals(lastCommit.getParentIds().get(1), repoCommit.getId());
        assertEquals("Transaction Author", lastCommit.getAuthor().getName().get());
        assertEquals("transaction@author.com", lastCommit.getAuthor().getEmail().get());
        assertEquals(logs.next(), repoCommit);
        assertEquals(logs.next(), transactionCommit);
        assertEquals(logs.next(), firstCommit);
        assertFalse(logs.hasNext());

    }

    @Test
    public void testMultipleTransaction() throws Exception {

        // make a commit
        insertAndAdd(points1);
        RevCommit mainCommit = geogit.command(CommitOp.class).setMessage("Commit1").call();

        // start the first transaction
        GeogitTransaction transaction1 = geogit.command(TransactionBegin.class).call();

        // perform a commit in the transaction
        insertAndAdd(transaction1, points2);
        RevCommit transaction1Commit = transaction1.command(CommitOp.class).setMessage("Commit2")
                .call();

        // Verify that the base repository is unchanged
        Iterator<RevCommit> logs = geogit.command(LogOp.class).call();
        assertEquals(mainCommit, logs.next());
        assertFalse(logs.hasNext());

        // Verify that the transaction has the commit
        logs = transaction1.command(LogOp.class).call();
        assertEquals(transaction1Commit, logs.next());
        assertEquals(mainCommit, logs.next());
        assertFalse(logs.hasNext());

        // start the second transaction
        GeogitTransaction transaction2 = geogit.command(TransactionBegin.class).call();

        // perform a commit in the transaction
        insertAndAdd(transaction2, points3);
        RevCommit transaction2Commit = transaction2.command(CommitOp.class).setMessage("Commit3")
                .call();

        // Verify that the base repository is unchanged
        logs = geogit.command(LogOp.class).call();
        assertEquals(mainCommit, logs.next());
        assertFalse(logs.hasNext());

        // Verify that the transaction has the commit
        logs = transaction2.command(LogOp.class).call();
        assertEquals(transaction2Commit, logs.next());
        assertEquals(mainCommit, logs.next());
        assertFalse(logs.hasNext());

        // Commit the first transaction
        geogit.command(TransactionEnd.class).setTransaction(transaction1).setRebase(true).call();

        // Verify that the base repository has the changes from the transaction
        logs = geogit.command(LogOp.class).call();
        assertEquals(transaction1Commit, logs.next());
        assertEquals(mainCommit, logs.next());
        assertFalse(logs.hasNext());

        // Now try to commit the second transaction
        geogit.command(TransactionEnd.class).setTransaction(transaction2).setRebase(true).call();

        // Verify that the base repository has the changes from the transaction
        logs = geogit.command(LogOp.class).call();
        RevCommit lastCommit = logs.next();
        assertFalse(lastCommit.equals(transaction2Commit));
        assertEquals(lastCommit.getMessage(), transaction2Commit.getMessage());
        assertEquals(lastCommit.getAuthor(), transaction2Commit.getAuthor());
        assertEquals(lastCommit.getCommitter().getName(), transaction2Commit.getCommitter()
                .getName());
        assertFalse(lastCommit.getCommitter().getTimestamp() == transaction2Commit.getCommitter()
                .getTimestamp());
        assertEquals(logs.next(), transaction1Commit);
        assertEquals(logs.next(), mainCommit);
        assertFalse(logs.hasNext());

    }

    @Test
    public void testConflictIsolation() throws Exception {
        insertAndAdd(points2);
        geogit.command(CommitOp.class).setMessage("foo").call();
        geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch1").call();
        insertAndAdd(points1);
        RevCommit mainCommit = geogit.command(CommitOp.class).setMessage("Commit1").call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        insertAndAdd(points1_modified);
        RevCommit modifiedCommit = geogit.command(CommitOp.class).setMessage("Commit2").call();
        GeogitTransaction tx = geogit.command(TransactionBegin.class).call();
        try {
            tx.command(MergeOp.class).addCommit(Suppliers.ofInstance(mainCommit.getId())).call();
            fail("Expected a merge conflict!");
        } catch (org.geogit.api.porcelain.MergeConflictsException e) {
            // expected.
        }
        List<Conflict> txConflicts = tx.command(ConflictsReadOp.class).call();
        List<Conflict> baseConflicts = geogit.command(ConflictsReadOp.class).call();
        assertTrue("There should be no conflicts outside the transaction", baseConflicts.size() == 0);
        assertTrue("There should be conflicts in the transaction", txConflicts.size() != 0);
    }

    @Test
    public void testBranchCreateCollision() throws Exception {

        // make a commit
        insertAndAdd(points1);
        RevCommit mainCommit = geogit.command(CommitOp.class).setMessage("Commit1").call();

        // start the first transaction
        GeogitTransaction transaction1 = geogit.command(TransactionBegin.class).call();

        // make a new branch
        transaction1.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch1").call();

        // perform a commit in the transaction
        insertAndAdd(transaction1, points2);
        RevCommit transaction1Commit = transaction1.command(CommitOp.class).setMessage("Commit2")
                .call();

        // Verify that the base repository is unchanged
        Iterator<RevCommit> logs = geogit.command(LogOp.class).call();
        assertEquals(logs.next(), mainCommit);
        assertFalse(logs.hasNext());

        // Verify that the transaction has the commit
        logs = transaction1.command(LogOp.class).call();
        assertEquals(logs.next(), transaction1Commit);
        assertEquals(logs.next(), mainCommit);
        assertFalse(logs.hasNext());

        // start the second transaction
        GeogitTransaction transaction2 = geogit.command(TransactionBegin.class).call();

        // make a new branch
        transaction2.command(BranchCreateOp.class).setAutoCheckout(true).setName("branch1").call();

        // perform a commit in the transaction
        insertAndAdd(transaction2, points3);
        RevCommit transaction2Commit = transaction2.command(CommitOp.class).setMessage("Commit3")
                .call();

        // Verify that the base repository is unchanged
        logs = geogit.command(LogOp.class).call();
        assertEquals(logs.next(), mainCommit);
        assertFalse(logs.hasNext());

        // Verify that the transaction has the commit
        logs = transaction2.command(LogOp.class).call();
        assertEquals(logs.next(), transaction2Commit);
        assertEquals(logs.next(), mainCommit);
        assertFalse(logs.hasNext());

        // Commit the first transaction
        geogit.command(TransactionEnd.class).setTransaction(transaction1).setRebase(true).call();

        // Verify that the base repository has the changes from the transaction
        logs = geogit.command(LogOp.class).call();
        assertEquals(logs.next(), mainCommit);
        assertFalse(logs.hasNext());

        geogit.command(CheckoutOp.class).setSource("branch1").call();
        logs = geogit.command(LogOp.class).call();
        assertEquals(logs.next(), transaction1Commit);
        assertEquals(logs.next(), mainCommit);
        assertFalse(logs.hasNext());

        // Now try to commit the second transaction
        geogit.command(TransactionEnd.class).setTransaction(transaction2).setRebase(true).call();

        // Verify that the base repository has the changes from the transaction
        logs = geogit.command(LogOp.class).call();
        RevCommit lastCommit = logs.next();
        assertFalse(lastCommit.equals(transaction2Commit));
        assertEquals(lastCommit.getMessage(), transaction2Commit.getMessage());
        assertEquals(lastCommit.getAuthor(), transaction2Commit.getAuthor());
        assertEquals(lastCommit.getCommitter().getName(), transaction2Commit.getCommitter()
                .getName());
        assertFalse(lastCommit.getCommitter().getTimestamp() == transaction2Commit.getCommitter()
                .getTimestamp());
        assertEquals(logs.next(), transaction1Commit);
        assertEquals(logs.next(), mainCommit);
        assertFalse(logs.hasNext());

    }

    @Test
    public void testCancelTransaction() throws Exception {
        LinkedList<RevCommit> expectedMain = new LinkedList<RevCommit>();

        // make a commit
        insertAndAdd(points1);
        RevCommit commit = geogit.command(CommitOp.class).call();
        expectedMain.addFirst(commit);

        // start a transaction
        GeogitTransaction t = geogit.command(TransactionBegin.class).call();

        // perform a commit in the transaction
        insertAndAdd(t, points2);
        commit = t.command(CommitOp.class).call();

        // Verify that the base repository is unchanged
        Iterator<RevCommit> logs = geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMain, logged);

        // Cancel the transaction
        geogit.command(TransactionEnd.class).setCancel(true).setTransaction(t).call();

        // Verify that the base repository is unchanged
        logs = geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expectedMain, logged);

    }

    @Test
    public void testEndNoTransaction() throws Exception {
        exception.expect(IllegalArgumentException.class);
        geogit.command(TransactionEnd.class).call();
    }

    @Test
    public void testEndWithinTransaction() throws Exception {
        // make a commit
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();

        // start a transaction
        GeogitTransaction t = geogit.command(TransactionBegin.class).call();

        // perform a commit in the transaction
        insertAndAdd(t, points2);
        t.command(CommitOp.class).call();

        // End the transaction
        exception.expect(IllegalStateException.class);
        t.command(TransactionEnd.class).setTransaction(t).call();

    }

    @Test
    public void testBeginWithinTransaction() throws Exception {
        // make a commit
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();

        // start a transaction
        GeogitTransaction t = geogit.command(TransactionBegin.class).call();

        // start a transaction within the transaction
        exception.expect(IllegalStateException.class);
        t.command(TransactionBegin.class).call();

    }

}
