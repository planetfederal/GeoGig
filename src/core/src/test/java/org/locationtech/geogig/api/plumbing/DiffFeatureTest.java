/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing;

import org.junit.Test;
import org.locationtech.geogig.api.NodeRef;
import org.locationtech.geogig.api.plumbing.diff.FeatureDiff;
import org.locationtech.geogig.api.porcelain.FeatureNodeRefFromRefspec;
import org.locationtech.geogig.test.integration.RepositoryTestCase;

import com.google.common.base.Suppliers;

public class DiffFeatureTest extends RepositoryTestCase {

    @Override
    protected void setUpInternal() throws Exception {
        populate(true, points1);
        insert(points1_modified);
    }

    @Test
    public void testDiffBetweenEditedFeatures() {
        NodeRef oldRef = geogig.command(FeatureNodeRefFromRefspec.class)
                .setRefspec("HEAD:" + NodeRef.appendChild(pointsName, idP1)).call().orNull();
        NodeRef newRef = geogig.command(FeatureNodeRefFromRefspec.class)
                .setRefspec(NodeRef.appendChild(pointsName, idP1)).call().orNull();
        FeatureDiff diff = geogig.command(DiffFeature.class)
                .setOldVersion(Suppliers.ofInstance(oldRef))
                .setNewVersion(Suppliers.ofInstance(newRef)).call();
        assertTrue(diff.hasDifferences());
        System.out.println(diff);
    }

    @Test
    public void testDiffBetweenFeatureAndItself() {
        NodeRef oldRef = geogig.command(FeatureNodeRefFromRefspec.class)
                .setRefspec(NodeRef.appendChild(pointsName, idP1)).call().orNull();
        NodeRef newRef = geogig.command(FeatureNodeRefFromRefspec.class)
                .setRefspec(NodeRef.appendChild(pointsName, idP1)).call().orNull();
        FeatureDiff diff = geogig.command(DiffFeature.class)
                .setOldVersion(Suppliers.ofInstance(oldRef))
                .setNewVersion(Suppliers.ofInstance(newRef)).call();
        assertFalse(diff.hasDifferences());
        System.out.println(diff);
    }

    @Test
    public void testDiffUnexistentFeature() {
        try {
            NodeRef oldRef = geogig.command(FeatureNodeRefFromRefspec.class)
                    .setRefspec(NodeRef.appendChild(pointsName, "Points.100")).call().orNull();
            NodeRef newRef = geogig.command(FeatureNodeRefFromRefspec.class)
                    .setRefspec(NodeRef.appendChild(pointsName, idP1)).call().orNull();
            geogig.command(DiffFeature.class).setOldVersion(Suppliers.ofInstance(oldRef))
                    .setNewVersion(Suppliers.ofInstance(newRef)).call();
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testDiffWrongPath() {
        try {
            NodeRef oldRef = geogig.command(FeatureNodeRefFromRefspec.class).setRefspec(pointsName)
                    .call().orNull();
            NodeRef newRef = geogig.command(FeatureNodeRefFromRefspec.class)
                    .setRefspec(NodeRef.appendChild(pointsName, idP1)).call().orNull();
            geogig.command(DiffFeature.class).setOldVersion(Suppliers.ofInstance(oldRef))
                    .setNewVersion(Suppliers.ofInstance(newRef)).call();
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

}
