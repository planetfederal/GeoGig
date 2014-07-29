/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api;

import org.junit.Test;
import org.locationtech.geogig.test.integration.RepositoryTestCase;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

public class FeatureBuilderTest extends RepositoryTestCase {

    @Override
    protected void setUpInternal() throws Exception {
        injector.configDatabase().put("user.name", "groldan");
        injector.configDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Test
    public void testFeatureBuilder() {
        FeatureBuilder builder = new FeatureBuilder(pointsType);
        RevFeature point1 = RevFeatureBuilder.build(points1);

        Feature test = builder.build(idP1, point1);

        // assertEquals(points1.getValue(), test.getValue());
        assertEquals(points1.getName(), test.getName());
        assertEquals(points1.getIdentifier(), test.getIdentifier());
        assertEquals(points1.getType(), test.getType());
        assertEquals(points1.getUserData(), test.getUserData());

        RevFeature feature = RevFeatureBuilder.build(test);
        Feature test2 = builder.build(idP1, feature);

        assertEquals(((SimpleFeature) test).getAttributes(),
                ((SimpleFeature) test2).getAttributes());
    }
}
