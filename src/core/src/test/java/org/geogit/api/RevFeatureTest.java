/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import static org.junit.Assert.assertEquals;

import org.geogit.api.RevObject.TYPE;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class RevFeatureTest {

    @Test
    public void testRevFeatureConstructorAndAccessors() {
        ImmutableList<Optional<Object>> values = ImmutableList.of(
                Optional.of((Object) "StringProp1_1"), Optional.of((Object) new Integer(1000)),
                Optional.of((Object) "POINT(1 1)"));

        RevFeature feature = RevFeatureImpl.build(values);

        assertEquals(TYPE.FEATURE, feature.getType());

        assertEquals(values, feature.getValues());
    }

    @Test
    public void testRevFeatureToString() {
        ImmutableList<Optional<Object>> values = ImmutableList.of(
                Optional.of((Object) "StringProp1_1"), Optional.of((Object) new Integer(1000)),
                Optional.of((Object) "POINT(1 1)"));

        RevFeature feature = RevFeatureImpl.build(values);

        String featureString = feature.toString();

        assertEquals("Feature[" + feature.getId().toString() + "; StringProp, 1000, POINT(1 1)]",
                featureString);
    }
}
