/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.repository;

import org.geogit.api.RevFeature;
import org.geogit.api.RevFeatureBuilder;
import org.geogit.test.integration.RepositoryTestCase;
import org.junit.Test;
import org.opengis.feature.Property;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class RevFeatureBuilderTest extends RepositoryTestCase {

    @Override
    protected void setUpInternal() throws Exception {

    }

    @Test
    public void testBuildEmpty() throws Exception {
        try {
            RevFeatureBuilder.build(null);
            fail("expected IllegalStateException on null feature");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("No feature set"));
        }
    }

    @Test
    public void testBuildFull() throws Exception {
        RevFeature feature = RevFeatureBuilder.build(points1);

        ImmutableList<Optional<Object>> values = feature.getValues();

        assertEquals(values.size(), points1.getProperties().size());

        for (Property prop : points1.getProperties()) {
            assertTrue(values.contains(Optional.fromNullable(prop.getValue())));
        }

        RevFeature feature2 = RevFeatureBuilder.build(lines1);

        values = feature2.getValues();

        assertEquals(values.size(), lines1.getProperties().size());

        for (Property prop : lines1.getProperties()) {
            assertTrue(values.contains(Optional.fromNullable(prop.getValue())));
        }

    }
}
