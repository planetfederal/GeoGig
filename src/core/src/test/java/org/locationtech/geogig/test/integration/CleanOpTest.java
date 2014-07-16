/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.locationtech.geogig.api.plumbing.DiffWorkTree;
import org.locationtech.geogig.api.plumbing.diff.DiffEntry;
import org.locationtech.geogig.api.porcelain.CleanOp;

import com.google.common.collect.Lists;

public class CleanOpTest extends RepositoryTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
    }

    @Test
    public void testClean() throws Exception {

        insert(points1, points2, points3);

        geogit.command(CleanOp.class).call();
        Iterator<DiffEntry> deleted = geogit.command(DiffWorkTree.class).call();
        ArrayList<DiffEntry> list = Lists.newArrayList(deleted);
        // Check that all the features have been deleted
        assertEquals(0, list.size());
    }

    @Test
    public void testTreeClean() throws Exception {

        insert(points1, points2, points3, lines1);

        geogit.command(CleanOp.class).setPath(pointsName).call();
        Iterator<DiffEntry> deleted = geogit.command(DiffWorkTree.class).call();
        ArrayList<DiffEntry> list = Lists.newArrayList(deleted);
        // Check that all the point features have been deleted but not the line one
        assertEquals(1, list.size());

    }

    @Test
    public void testUnexistentPathRemoval() throws Exception {

        populate(false, points1, points2, points3);

        try {
            geogit.command(CleanOp.class).setPath(linesName).call();
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

    }

}
