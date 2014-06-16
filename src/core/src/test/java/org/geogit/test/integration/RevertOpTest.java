/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import java.util.Iterator;
import java.util.List;

import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.RevFeatureBuilder;
import org.geogit.api.RevTree;
import org.geogit.api.plumbing.FindTreeChild;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.ResolveTreeish;
import org.geogit.api.plumbing.merge.Conflict;
import org.geogit.api.plumbing.merge.ConflictsReadOp;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.ConfigOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.api.porcelain.LogOp;
import org.geogit.api.porcelain.RevertConflictsException;
import org.geogit.api.porcelain.RevertOp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.feature.Feature;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;

public class RevertOpTest extends RepositoryTestCase {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        // These values should be used during a commit to set author/committer
        // TODO: author/committer roles need to be defined better, but for
        // now they are the same thing.
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET).setName("user.name")
                .setValue("groldan").call();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET).setName("user.email")
                .setValue("groldan@opengeo.org").call();
    }

    @Test
    public void testRevert() throws Exception {
        ObjectId oId1 = insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        insertAndAdd(points2);
        RevCommit c2 = geogit.command(CommitOp.class).setMessage("commit for " + idP2).call();

        insertAndAdd(points1_modified);
        RevCommit c3 = geogit.command(CommitOp.class).setMessage("commit for modified " + idP1)
                .call();

        ObjectId oId3 = insertAndAdd(points3);
        RevCommit c4 = geogit.command(CommitOp.class).setMessage("commit for " + idP3).call();

        deleteAndAdd(points3);
        RevCommit c5 = geogit.command(CommitOp.class).setMessage("commit for deleted " + idP3)
                .call();

        // revert Points.2 add, Points.1 change, and Points.3 delete
        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c2.getId()))
                .addCommit(Suppliers.ofInstance(c3.getId()))
                .addCommit(Suppliers.ofInstance(c5.getId())).call();

        final Optional<Ref> currHead = geogit.command(RefParse.class).setName(Ref.HEAD).call();

        final Optional<ObjectId> headTreeId = geogit.command(ResolveTreeish.class)
                .setTreeish(currHead.get().getObjectId()).call();

        RevTree headTree = repo.getTree(headTreeId.get());

        Optional<NodeRef> points1Node = geogit.command(FindTreeChild.class)
                .setChildPath(NodeRef.appendChild(pointsName, idP1)).setParent(headTree).call();

        assertTrue(points1Node.isPresent());
        assertEquals(oId1, points1Node.get().getNode().getObjectId());

        Optional<NodeRef> points2Node = geogit.command(FindTreeChild.class)
                .setChildPath(NodeRef.appendChild(pointsName, idP2)).setParent(headTree).call();

        assertFalse(points2Node.isPresent());

        Optional<NodeRef> points3Node = geogit.command(FindTreeChild.class)
                .setChildPath(NodeRef.appendChild(pointsName, idP3)).setParent(headTree).call();

        assertTrue(points3Node.isPresent());
        assertEquals(oId3, points3Node.get().getNode().getObjectId());

        Iterator<RevCommit> log = geogit.command(LogOp.class).call();

        // There should be 3 new commits, followed by all of the previous commits.
        log.next();
        log.next();
        log.next();

        assertEquals(c5.getId(), log.next().getId());
        assertEquals(c4.getId(), log.next().getId());
        assertEquals(c3.getId(), log.next().getId());
        assertEquals(c2.getId(), log.next().getId());
        assertEquals(c1.getId(), log.next().getId());
    }

    @Test
    public void testRevertWithoutCommit() throws Exception {
        ObjectId oId1 = insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        insertAndAdd(points2);
        RevCommit c2 = geogit.command(CommitOp.class).setMessage("commit for " + idP2).call();

        insertAndAdd(points1_modified);
        RevCommit c3 = geogit.command(CommitOp.class).setMessage("commit for modified " + idP1)
                .call();

        ObjectId oId3 = insertAndAdd(points3);
        RevCommit c4 = geogit.command(CommitOp.class).setMessage("commit for " + idP3).call();

        deleteAndAdd(points3);
        RevCommit c5 = geogit.command(CommitOp.class).setMessage("commit for deleted " + idP3)
                .call();

        // revert Points.2 add, Points.1 change, and Points.3 delete
        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c2.getId()))
                .addCommit(Suppliers.ofInstance(c3.getId()))
                .addCommit(Suppliers.ofInstance(c5.getId())).setCreateCommit(false).call();

        final Optional<Ref> currHead = geogit.command(RefParse.class).setName(Ref.WORK_HEAD).call();

        final Optional<ObjectId> headTreeId = geogit.command(ResolveTreeish.class)
                .setTreeish(currHead.get().getObjectId()).call();

        RevTree headTree = repo.getTree(headTreeId.get());

        Optional<NodeRef> points1Node = geogit.command(FindTreeChild.class).setIndex(true)
                .setChildPath(NodeRef.appendChild(pointsName, idP1)).setParent(headTree).call();

        assertTrue(points1Node.isPresent());
        assertEquals(oId1, points1Node.get().getNode().getObjectId());

        Optional<NodeRef> points2Node = geogit.command(FindTreeChild.class).setIndex(true)
                .setChildPath(NodeRef.appendChild(pointsName, idP2)).setParent(headTree).call();

        assertFalse(points2Node.isPresent());

        Optional<NodeRef> points3Node = geogit.command(FindTreeChild.class).setIndex(true)
                .setChildPath(NodeRef.appendChild(pointsName, idP3)).setParent(headTree).call();

        assertTrue(points3Node.isPresent());
        assertEquals(oId3, points3Node.get().getNode().getObjectId());

        Iterator<RevCommit> log = geogit.command(LogOp.class).call();

        // There should only the old commits.

        assertEquals(c5.getId(), log.next().getId());
        assertEquals(c4.getId(), log.next().getId());
        assertEquals(c3.getId(), log.next().getId());
        assertEquals(c2.getId(), log.next().getId());
        assertEquals(c1.getId(), log.next().getId());
    }

    @Test
    public void testHeadWithNoHistory() throws Exception {
        exception.expect(IllegalStateException.class);
        geogit.command(RevertOp.class).call();
    }

    @Test
    public void testUncleanWorkingTree() throws Exception {
        insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        insert(points2);
        exception.expect(IllegalStateException.class);
        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c1.getId())).call();
    }

    @Test
    public void testUncleanIndex() throws Exception {
        insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        insertAndAdd(points2);
        exception.expect(IllegalStateException.class);
        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c1.getId())).call();
    }

    @Test
    public void testRevertOnlyCommit() throws Exception {
        insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();

        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c1.getId())).call();

        final Optional<Ref> currHead = geogit.command(RefParse.class).setName(Ref.HEAD).call();

        final Optional<ObjectId> headTreeId = geogit.command(ResolveTreeish.class)
                .setTreeish(currHead.get().getObjectId()).call();

        RevTree headTree = repo.getTree(headTreeId.get());

        Optional<NodeRef> points1Node = geogit.command(FindTreeChild.class)
                .setChildPath(NodeRef.appendChild(pointsName, idP1)).setParent(headTree).call();

        assertFalse(points1Node.isPresent());
    }

    @Test
    public void testNoUserNameForResolveCommiter() throws Exception {
        insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET).setName("user.name")
                .setValue(null).call();
        exception.expect(IllegalStateException.class);
        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c1.getId())).call();
    }

    @Test
    public void testNoUserEmailForResolveCommiterEmail() throws Exception {
        insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET).setName("user.email")
                .setValue(null).call();
        exception.expect(IllegalStateException.class);
        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c1.getId())).call();
    }

    @Test
    public void testRevertToWrongCommit() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        exception.expect(IllegalArgumentException.class);
        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(ObjectId.forString("wrong")))
                .call();
    }

    @Test
    public void testRevertUsingContinueAndAbort() throws Exception {
        insertAndAdd(points1);
        RevCommit commit = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        exception.expect(IllegalArgumentException.class);
        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(commit.getId()))
                .setAbort(true).setContinue(true).call();
    }

    @Test
    public void testStillDeletedMergeConflictResolution() throws Exception {
        insertAndAdd(points1);
        @SuppressWarnings("unused")
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        deleteAndAdd(points1);
        RevCommit c2 = geogit.command(CommitOp.class).setMessage("commit for removing " + idP1)
                .call();
        @SuppressWarnings("unused")
        ObjectId oId1 = insertAndAdd(points1);
        @SuppressWarnings("unused")
        RevCommit c3 = geogit.command(CommitOp.class).setMessage("commit for " + idP1 + " again")
                .call();
        try {
            geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c2.getId())).call();
            fail();
        } catch (RevertConflictsException e) {
            assertTrue(e.getMessage().contains(idP1));
        }

    }

    @Test
    public void testRevertToSameFeatureIsNotConflict() throws Exception {
        @SuppressWarnings("unused")
        ObjectId oId1 = insertAndAdd(points1);
        @SuppressWarnings("unused")
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        insertAndAdd(points1_modified);
        RevCommit c2 = geogit.command(CommitOp.class).setMessage("commit for modified " + idP1)
                .call();
        insertAndAdd(points1);
        @SuppressWarnings("unused")
        RevCommit c3 = geogit.command(CommitOp.class)
                .setMessage("commit for modified " + idP1 + " again").call();

        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c2.getId())).call();

    }

    @Test
    public void testRevertModifiedFeatureConflictSolveAndContinue() throws Exception {
        ObjectId oId1 = insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        insertAndAdd(points1_modified);
        RevCommit c2 = geogit.command(CommitOp.class).setMessage("commit for modified " + idP1)
                .call();
        Feature points1_modifiedB = feature(pointsType, idP1, "StringProp1_2", new Integer(2000),
                "POINT(1 1)");
        insertAndAdd(points1_modifiedB);
        RevCommit c3 = geogit.command(CommitOp.class)
                .setMessage("commit for modified " + idP1 + " again").call();
        try {
            geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c2.getId())).call();
            fail();
        } catch (RevertConflictsException e) {
            assertTrue(e.getMessage().contains(idP1));
        }

        Optional<Ref> ref = geogit.command(RefParse.class).setName(Ref.ORIG_HEAD).call();
        assertTrue(ref.isPresent());
        assertEquals(c3.getId(), ref.get().getObjectId());

        List<Conflict> conflicts = geogit.command(ConflictsReadOp.class).call();
        assertEquals(1, conflicts.size());
        String path = NodeRef.appendChild(pointsName, idP1);
        assertEquals(conflicts.get(0).getPath(), path);
        assertEquals(conflicts.get(0).getOurs(), RevFeatureBuilder.build(points1_modifiedB).getId());
        assertEquals(conflicts.get(0).getTheirs(), RevFeatureBuilder.build(points1).getId());

        // solve, and continue
        insert(points1);
        geogit.command(AddOp.class).call();
        geogit.command(RevertOp.class).setContinue(true).call();

        Iterator<RevCommit> log = geogit.command(LogOp.class).call();
        RevCommit logCommit = log.next();
        assertEquals(c2.getAuthor().getName(), logCommit.getAuthor().getName());
        assertEquals(c2.getCommitter().getName(), logCommit.getCommitter().getName());
        assertEquals("Revert '" + c2.getMessage() + "'\nThis reverts " + c2.getId().toString(),
                logCommit.getMessage());
        assertNotSame(c2.getCommitter().getTimestamp(), logCommit.getCommitter().getTimestamp());
        assertNotSame(c2.getTreeId(), logCommit.getTreeId());

        final Optional<Ref> currHead = geogit.command(RefParse.class).setName(Ref.HEAD).call();

        final Optional<ObjectId> headTreeId = geogit.command(ResolveTreeish.class)
                .setTreeish(currHead.get().getObjectId()).call();
        RevTree headTree = repo.getTree(headTreeId.get());
        Optional<NodeRef> points1Node = geogit.command(FindTreeChild.class)
                .setChildPath(NodeRef.appendChild(pointsName, idP1)).setParent(headTree).call();
        assertTrue(points1Node.isPresent());
        assertEquals(oId1, points1Node.get().getNode().getObjectId());

        assertEquals(c3.getId(), log.next().getId());
        assertEquals(c2.getId(), log.next().getId());
        assertEquals(c1.getId(), log.next().getId());
        assertFalse(log.hasNext());
    }

    @Test
    public void testRevertModifiedFeatureConflictAndAbort() throws Exception {
        insertAndAdd(points1);
        @SuppressWarnings("unused")
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        insertAndAdd(points1_modified);
        RevCommit c2 = geogit.command(CommitOp.class).setMessage("commit for modified " + idP1)
                .call();
        Feature points1_modifiedB = feature(pointsType, idP1, "StringProp1_2", new Integer(2000),
                "POINT(1 1)");
        ObjectId oId = insertAndAdd(points1_modifiedB);
        RevCommit c3 = geogit.command(CommitOp.class)
                .setMessage("commit for modified " + idP1 + " again").call();
        try {
            geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c2.getId())).call();
            fail();
        } catch (RevertConflictsException e) {
            assertTrue(e.getMessage().contains(idP1));
        }

        Optional<Ref> ref = geogit.command(RefParse.class).setName(Ref.ORIG_HEAD).call();
        assertTrue(ref.isPresent());
        assertEquals(c3.getId(), ref.get().getObjectId());

        List<Conflict> conflicts = geogit.command(ConflictsReadOp.class).call();
        assertEquals(1, conflicts.size());
        String path = NodeRef.appendChild(pointsName, idP1);
        assertEquals(conflicts.get(0).getPath(), path);
        assertEquals(conflicts.get(0).getOurs(), RevFeatureBuilder.build(points1_modifiedB).getId());
        assertEquals(conflicts.get(0).getTheirs(), RevFeatureBuilder.build(points1).getId());

        geogit.command(RevertOp.class).setAbort(true).call();

        final Optional<Ref> currHead = geogit.command(RefParse.class).setName(Ref.HEAD).call();

        final Optional<ObjectId> headTreeId = geogit.command(ResolveTreeish.class)
                .setTreeish(currHead.get().getObjectId()).call();
        RevTree headTree = repo.getTree(headTreeId.get());
        Optional<NodeRef> points1Node = geogit.command(FindTreeChild.class)
                .setChildPath(NodeRef.appendChild(pointsName, idP1)).setParent(headTree).call();
        assertTrue(points1Node.isPresent());
        assertEquals(oId, points1Node.get().getNode().getObjectId());

    }

    @Test
    public void testRevertEntireFeatureTypeTree() throws Exception {
        insertAndAdd(points1);
        RevCommit c1 = geogit.command(CommitOp.class).setMessage("commit for " + idP1).call();
        insertAndAdd(points2);
        RevCommit c2 = geogit.command(CommitOp.class).setMessage("commit for " + idP2).call();
        insertAndAdd(points3);
        RevCommit c3 = geogit.command(CommitOp.class).setMessage("commit for " + idP3).call();
        insertAndAdd(lines1);
        RevCommit c4 = geogit.command(CommitOp.class).setMessage("commit for " + idL1).call();

        geogit.command(RevertOp.class).addCommit(Suppliers.ofInstance(c4.getId())).call();

        final Optional<Ref> currHead = geogit.command(RefParse.class).setName(Ref.HEAD).call();

        final Optional<ObjectId> headTreeId = geogit.command(ResolveTreeish.class)
                .setTreeish(currHead.get().getObjectId()).call();

        RevTree headTree = repo.getTree(headTreeId.get());

        Optional<NodeRef> lines1Node = geogit.command(FindTreeChild.class)
                .setChildPath(NodeRef.appendChild(linesName, idL1)).setParent(headTree).call();

        assertFalse(lines1Node.isPresent());

        @SuppressWarnings("unused")
        Optional<NodeRef> linesNode = geogit.command(FindTreeChild.class).setChildPath(linesName)
                .setParent(headTree).call();

        // assertFalse(linesNode.isPresent());

        Iterator<RevCommit> log = geogit.command(LogOp.class).call();
        log.next();
        assertEquals(c4.getId(), log.next().getId());
        assertEquals(c3.getId(), log.next().getId());
        assertEquals(c2.getId(), log.next().getId());
        assertEquals(c1.getId(), log.next().getId());
        assertFalse(log.hasNext());
    }
}
