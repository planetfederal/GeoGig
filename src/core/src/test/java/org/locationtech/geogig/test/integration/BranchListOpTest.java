/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.porcelain.BranchCreateOp;
import org.locationtech.geogig.api.porcelain.BranchListOp;
import org.locationtech.geogig.api.porcelain.CheckoutOp;
import org.locationtech.geogig.api.porcelain.CloneOp;
import org.locationtech.geogig.api.porcelain.CommitOp;
import org.locationtech.geogig.remote.RemoteRepositoryTestCase;

import com.google.common.collect.ImmutableList;

public class BranchListOpTest extends RemoteRepositoryTestCase {

    @Override
    protected void setUpInternal() throws Exception {
        // Commit several features to the remote

        insertAndAdd(remoteGeogit.geogit, points1);
        remoteGeogit.geogit.command(CommitOp.class).call();

        // Create and checkout branch1
        remoteGeogit.geogit.command(BranchCreateOp.class).setAutoCheckout(true).setName("Branch1")
                .call();

        // Commit some changes to branch1
        insertAndAdd(remoteGeogit.geogit, points2);
        remoteGeogit.geogit.command(CommitOp.class).call();

        insertAndAdd(remoteGeogit.geogit, points3);
        remoteGeogit.geogit.command(CommitOp.class).call();

        // Checkout master and commit some changes
        remoteGeogit.geogit.command(CheckoutOp.class).setSource("master").call();

        insertAndAdd(remoteGeogit.geogit, lines1);
        remoteGeogit.geogit.command(CommitOp.class).call();

        insertAndAdd(remoteGeogit.geogit, lines2);
        remoteGeogit.geogit.command(CommitOp.class).call();

        // clone from the remote
        CloneOp clone = clone();
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).setBranch("Branch1").call();
    }

    @Test
    public void testBranchListOp() throws Exception {

        ImmutableList<Ref> branches = remoteGeogit.geogit.command(BranchListOp.class)
                .setLocal(true).setRemotes(false).call();

        assertEquals(Ref.HEADS_PREFIX + "Branch1", branches.get(0).getName());
        assertEquals(Ref.HEADS_PREFIX + "master", branches.get(1).getName());
    }

    @Test
    public void testRemoteListing() throws Exception {

        ImmutableList<Ref> branches = localGeogit.geogit.command(BranchListOp.class).setLocal(true)
                .setRemotes(true).call();

        assertEquals(Ref.HEADS_PREFIX + "Branch1", branches.get(0).getName());
        assertEquals(Ref.HEADS_PREFIX + "master", branches.get(1).getName());
        assertEquals(Ref.REMOTES_PREFIX + "origin/Branch1", branches.get(2).getName());
        assertEquals(Ref.REMOTES_PREFIX + "origin/HEAD", branches.get(3).getName());
        assertEquals(Ref.REMOTES_PREFIX + "origin/master", branches.get(4).getName());
    }
}
