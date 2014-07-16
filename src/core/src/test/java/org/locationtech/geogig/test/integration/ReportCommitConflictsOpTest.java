/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration;

import org.junit.Test;
import org.locationtech.geogig.api.RevCommit;
import org.locationtech.geogig.api.plumbing.merge.MergeScenarioReport;
import org.locationtech.geogig.api.plumbing.merge.ReportCommitConflictsOp;
import org.locationtech.geogig.api.porcelain.AddOp;
import org.locationtech.geogig.api.porcelain.BranchCreateOp;
import org.locationtech.geogig.api.porcelain.CheckoutOp;
import org.locationtech.geogig.api.porcelain.CommitOp;
import org.opengis.feature.Feature;

public class ReportCommitConflictsOpTest extends RepositoryTestCase {

    @Override
    protected void setUpInternal() throws Exception {
    }

    @Test
    public void testAddedSameFeature() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        insertAndAdd(points2);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        insertAndAdd(points2);
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(0, conflicts.getConflicts().size());
        assertEquals(0, conflicts.getUnconflicted().size());
    }

    @Test
    public void testRemovedSameFeature() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        deleteAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        deleteAndAdd(points1);
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(0, conflicts.getConflicts().size());
        assertEquals(0, conflicts.getUnconflicted().size());
    }

    @Test
    public void testModifiedSameFeatureCompatible() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                "POINT(1 1)");
        insertAndAdd(points1Modified);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        Feature points1ModifiedB = feature(pointsType, idP1, "StringProp1_1", new Integer(2000),
                "POINT(1 1)");
        insertAndAdd(points1ModifiedB);
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(0, conflicts.getConflicts().size());
        assertEquals(1, conflicts.getUnconflicted().size());
    }

    @Test
    public void testModifiedAndNonExistant() throws Exception {
        insertAndAdd(points2);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                "POINT(1 1)");
        insertAndAdd(points1Modified);
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(0, conflicts.getConflicts().size());
        assertEquals(1, conflicts.getUnconflicted().size());
    }

    @Test
    public void testModifiedSameAttributeCompatible() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                "POINT(1 1)");
        insertAndAdd(points1Modified);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        Feature points1ModifiedB = feature(pointsType, idP1, "StringProp1_2", new Integer(2000),
                "POINT(1 1)");
        insertAndAdd(points1ModifiedB);
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(0, conflicts.getConflicts().size());
        assertEquals(1, conflicts.getUnconflicted().size());
    }

    @Test
    public void testModifiedSameFeatureIncompatible() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                "POINT(1 1)");
        insertAndAdd(points1Modified);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        Feature points1ModifiedB = feature(pointsType, idP1, "StringProp1_3", new Integer(1000),
                "POINT(1 1)");
        insertAndAdd(points1ModifiedB);
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(1, conflicts.getConflicts().size());
        assertEquals(0, conflicts.getUnconflicted().size());
    }

    @Test
    public void testModifiedAndRemoved() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                "POINT(1 1)");
        insertAndAdd(points1Modified);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        deleteAndAdd(points1);
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(1, conflicts.getConflicts().size());
        assertEquals(0, conflicts.getUnconflicted().size());
    }

    @Test
    public void testAddedDifferentFeatures() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        insertAndAdd(points2);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        insertAndAdd(points3);
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(0, conflicts.getConflicts().size());
        assertEquals(1, conflicts.getUnconflicted().size());
    }

    @Test
    public void testAddedSameFeatureType() throws Exception {
        insertAndAdd(lines1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        insert(points2);
        delete(points2);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        insert(points2);
        delete(points2);
        geogit.command(AddOp.class).call();
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(0, conflicts.getConflicts().size());
        assertEquals(0, conflicts.getUnconflicted().size());
    }

    @Test
    public void testAddedDifferentFeatureType() throws Exception {
        insertAndAdd(lines1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        insert(points1);
        delete(points1);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        insert(points1B);
        delete(points1B);
        geogit.command(AddOp.class).call();
        RevCommit branchCommit = geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("master").call();
        MergeScenarioReport conflicts = geogit.command(ReportCommitConflictsOp.class)
                .setCommit(branchCommit).call();
        assertEquals(1, conflicts.getConflicts().size());
        assertEquals(0, conflicts.getUnconflicted().size());
    }

}
