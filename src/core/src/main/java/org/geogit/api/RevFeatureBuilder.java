/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import java.util.Collection;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Provides a method of building a {@link RevFeature} from a {@link Feature}.
 * 
 * @see RevFeature
 * @see Feature
 */
public final class RevFeatureBuilder {

    private RevFeatureBuilder() {
        //
    }

    /**
     * Constructs a new {@link RevFeature} from the provided {@link Feature}.
     * 
     * @param feature the feature to build from
     * @return the newly constructed RevFeature
     */
    public static RevFeature build(Feature feature) {
        if (feature == null) {
            throw new IllegalStateException("No feature set");
        }

        Collection<Property> props = feature.getProperties();

        ImmutableList.Builder<Optional<Object>> valuesBuilder = new ImmutableList.Builder<Optional<Object>>();

        for (Property prop : props) {
            valuesBuilder.add(Optional.fromNullable(prop.getValue()));
        }

        return RevFeatureImpl.build(valuesBuilder.build());
    }
}
