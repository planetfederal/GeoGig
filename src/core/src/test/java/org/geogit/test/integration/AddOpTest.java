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
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevFeatureTypeImpl;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.plumbing.FindTreeChild;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.plumbing.diff.DiffEntry.ChangeType;
import org.geogit.api.plumbing.merge.Conflict;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.BranchCreateOp;
import org.geogit.api.porcelain.CheckoutOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.MergeConflictsException;
import org.geogit.api.porcelain.MergeOp;
import org.junit.Test;
import org.opengis.feature.Feature;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class AddOpTest extends RepositoryTestCase {

    @Override
    protected void setUpInternal() throws Exception {
        injector.configDatabase().put("user.name", "groldan");
        injector.configDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Test
    public void testAddSingleFile() throws Exception {
        insert(points1);
        List<DiffEntry> diffs = toList(repo.workingTree().getUnstaged(null));
        assertEquals(2, diffs.size());
        assertEquals(pointsName, diffs.get(0).newPath());
        assertEquals(NodeRef.appendChild(pointsName, idP1), diffs.get(1).newPath());
    }

    @Test
    public void testAddMultipleFeatures() throws Exception {
        insert(points1);
        insert(points2);
        insert(points3);
        geogit.command(AddOp.class).call();
        List<DiffEntry> unstaged = toList(repo.workingTree().getUnstaged(null));
        assertEquals(ImmutableList.of(), unstaged);
    }

    @Test
    public void testAddMultipleTimes() throws Exception {
        insert(points1);
        insert(points2);
        insert(points3);
        geogit.command(AddOp.class).call();
        Iterator<DiffEntry> iterator = repo.workingTree().getUnstaged(null);
        assertFalse(iterator.hasNext());
        insert(lines1);
        insert(lines2);
        geogit.command(AddOp.class).call();
        iterator = repo.workingTree().getUnstaged(null);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testAddNewPathUsingPathFilter() throws Exception {
        insert(points1);
        insert(points2);
        geogit.command(AddOp.class).addPattern("Points/Points.1").call();
        List<DiffEntry> unstaged = toList(repo.index().getStaged(null));
        assertEquals(unstaged.toString(), 2, unstaged.size());

        assertEquals(ChangeType.ADDED, unstaged.get(0).changeType());
        assertEquals(RevObject.TYPE.TREE, unstaged.get(0).getNewObject().getType());
        assertEquals("Points", unstaged.get(0).newName());
        RevFeatureType ft = RevFeatureTypeImpl.build(pointsType);
        ObjectId expectedTreeMdId = ft.getId();
        assertEquals(expectedTreeMdId, unstaged.get(0).getNewObject().getMetadataId());

        assertEquals(ChangeType.ADDED, unstaged.get(1).changeType());
        assertEquals(RevObject.TYPE.FEATURE, unstaged.get(1).getNewObject().getType());
        assertEquals("Points.1", unstaged.get(1).newName());
        assertFalse("feature node's metadata id should not be set, as it uses the parent tree one",
                unstaged.get(1).getNewObject().getNode().getMetadataId().isPresent());
    }

    @Test
    public void testAddMultipleFeaturesWithPathFilter() throws Exception {
        insert(points1);
        insert(points2);
        insert(lines1);
        geogit.command(AddOp.class).addPattern("Points").call();
        List<DiffEntry> unstaged = toList(repo.workingTree().getUnstaged(null));
        assertEquals(2, unstaged.size());
        assertEquals(linesName, unstaged.get(0).newName());
        assertEquals(ChangeType.ADDED, unstaged.get(0).changeType());
        assertEquals(TYPE.TREE, unstaged.get(0).getNewObject().getType());
    }

    @Test
    public void testAddSingleDeletion() throws Exception {
        insert(points1);
        insert(points2);
        geogit.command(AddOp.class).call();
        List<DiffEntry> staged = toList(repo.index().getStaged(Lists.newArrayList(pointsName)));
        assertEquals(3, staged.size());
        delete(points1);
        geogit.command(AddOp.class).call();
        staged = toList(repo.index().getStaged(Lists.newArrayList(pointsName)));
        assertEquals(2, staged.size());
    }

    @Test
    public void testAddTreeDeletion() throws Exception {
        insert(points1);
        insert(points2);
        geogit.command(AddOp.class).call();
        repo.workingTree().delete(pointsName);
        geogit.command(AddOp.class).call();
        List<DiffEntry> staged = toList(repo.index().getStaged(Lists.newArrayList(pointsName)));
        assertEquals(0, staged.size());
        assertEquals(0, repo.index().countStaged(null).featureCount());
        assertEquals(0, repo.index().countStaged(null).treeCount());
    }

    @Test
    public void testAddUpdate() throws Exception {
        insert(points1);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();

        insert(points1_modified);
        insert(lines1);
        geogit.command(AddOp.class).setUpdateOnly(true).call();
        List<DiffEntry> unstaged = toList(repo.workingTree().getUnstaged(null));
        assertEquals(2, unstaged.size());
        assertEquals(linesName, unstaged.get(0).newName());
        assertEquals(lines1.getIdentifier().getID(), unstaged.get(1).newName());
    }

    @Test
    public void testAddUpdateWithPathFilter() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        insert(points1_modified);
        insert(lines1);

        // stage only Lines changed
        geogit.command(AddOp.class).setUpdateOnly(true).addPattern(pointsName).call();
        List<DiffEntry> staged = toList(repo.index().getStaged(null));
        assertEquals(1, staged.size());
        assertEquals(idP1, staged.get(0).newName());

        List<DiffEntry> unstaged = toList(repo.workingTree().getUnstaged(null));

        assertEquals(2, unstaged.size());
        assertEquals(linesName, unstaged.get(0).newName());
        assertEquals(idL1, unstaged.get(1).newName());

        geogit.command(AddOp.class).setUpdateOnly(true).addPattern("Points").call();
        unstaged = toList(repo.workingTree().getUnstaged(null));

        assertEquals(2, unstaged.size());
        assertEquals(linesName, unstaged.get(0).newName());
        assertEquals(idL1, unstaged.get(1).newName());
    }

    @Test
    public void testInsertionAndAdditionFixesConflict() throws Exception {
        Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                "POINT(1 1)");
        Feature points1ModifiedB = feature(pointsType, idP1, "StringProp1_3", new Integer(2000),
                "POINT(1 1)");
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        insertAndAdd(points1Modified);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        insertAndAdd(points1ModifiedB);
        insertAndAdd(points2);
        geogit.command(CommitOp.class).call();

        geogit.command(CheckoutOp.class).setSource("master").call();
        Ref branch = geogit.command(RefParse.class).setName("TestBranch").call().get();
        try {
            geogit.command(MergeOp.class).addCommit(Suppliers.ofInstance(branch.getObjectId()))
                    .call();
            fail();
        } catch (MergeConflictsException e) {
            assertTrue(e.getMessage().contains("conflict"));
        }
        insert(points1);
        geogit.command(AddOp.class).call();
        List<Conflict> conflicts = geogit.getRepository().stagingDatabase()
                .getConflicts(null, null);
        assertTrue(conflicts.isEmpty());
        geogit.command(CommitOp.class).call();
        Optional<Ref> ref = geogit.command(RefParse.class).setName(Ref.MERGE_HEAD).call();
        assertFalse(ref.isPresent());
    }

    @Test
    public void testAdditionFixesConflict() throws Exception {
        Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                "POINT(1 1)");
        Feature points1ModifiedB = feature(pointsType, idP1, "StringProp1_3", new Integer(2000),
                "POINT(1 1)");
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        insertAndAdd(points1Modified);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        insertAndAdd(points1ModifiedB);
        insertAndAdd(points2);
        geogit.command(CommitOp.class).call();

        geogit.command(CheckoutOp.class).setSource("master").call();
        Ref branch = geogit.command(RefParse.class).setName("TestBranch").call().get();
        try {
            geogit.command(MergeOp.class).addCommit(Suppliers.ofInstance(branch.getObjectId()))
                    .call();
            fail();
        } catch (MergeConflictsException e) {
            assertTrue(true);
        }
        geogit.command(AddOp.class).call();
        List<Conflict> conflicts = geogit.getRepository().stagingDatabase()
                .getConflicts(null, null);
        assertTrue(conflicts.isEmpty());
        geogit.command(CommitOp.class).call();
        Optional<Ref> ref = geogit.command(RefParse.class).setName(Ref.MERGE_HEAD).call();
        assertFalse(ref.isPresent());
    }

    @Test
    public void testAddModifiedFeatureType() throws Exception {
        insertAndAdd(points2, points1B);
        geogit.command(CommitOp.class).call();
        geogit.getRepository().workingTree().updateTypeTree(pointsName, modifiedPointsType);
        geogit.command(AddOp.class).call();
        List<DiffEntry> list = toList(geogit.getRepository().index().getStaged(null));
        assertFalse(list.isEmpty());
        String path = NodeRef.appendChild(pointsName, idP1);
        Optional<NodeRef> ref = geogit.command(FindTreeChild.class).setChildPath(path)
                .setIndex(true).setParent(geogit.getRepository().index().getTree()).call();
        assertTrue(ref.isPresent());
        assertFalse(ref.get().getNode().getMetadataId().isPresent());
        path = NodeRef.appendChild(pointsName, idP2);
        ref = geogit.command(FindTreeChild.class).setChildPath(path).setIndex(true)
                .setParent(geogit.getRepository().index().getTree()).call();
        assertTrue(ref.isPresent());
        assertTrue(ref.get().getNode().getMetadataId().isPresent());

    }
}
