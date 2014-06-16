/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import org.geogit.api.NodeRef;
import org.geogit.api.hooks.GeoGitAPI;
import org.geogit.api.porcelain.CommitOp;
import org.junit.Test;
import org.opengis.feature.Feature;

public class GeogitAPITest extends RepositoryTestCase {

    private GeoGitAPI geogitAPI;

    @Override
    protected void setUpInternal() throws Exception {
        geogitAPI = new GeoGitAPI(this.repo);
    }

    @Test
    public void testGetFeaturesToCommit() throws Exception {
        insertAndAdd(points1, points2);
        Feature[] features = geogitAPI.getFeaturesToCommit(null, false);
        assertEquals(2, features.length);
    }

    @Test
    public void testGetFeatureFromHead() throws Exception {
        insertAndAdd(points1);
        geogit.command(CommitOp.class).setMessage("Commit message").call();
        Feature feature = geogitAPI.getFeatureFromHead(NodeRef.appendChild(pointsName, idP1));
        assertNotNull(feature);
    }

    @Test
    public void testGetFeatureFromWorkingTree() throws Exception {
        insert(points1);
        Feature feature = geogitAPI
                .getFeatureFromWorkingTree(NodeRef.appendChild(pointsName, idP1));
        assertNotNull(feature);
    }

}
