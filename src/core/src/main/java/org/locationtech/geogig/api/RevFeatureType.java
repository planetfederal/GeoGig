/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

import com.google.common.collect.ImmutableList;

public interface RevFeatureType extends RevObject {

    public abstract FeatureType type();

    /**
     * @return the sorted {@link PropertyDescriptor}s of the feature type
     */
    public abstract ImmutableList<PropertyDescriptor> sortedDescriptors();

    /**
     * @return the name of the feature type
     */
    public abstract Name getName();

}