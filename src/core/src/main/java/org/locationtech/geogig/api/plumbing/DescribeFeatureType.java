/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing;

import org.locationtech.geogig.api.AbstractGeoGigOp;
import org.locationtech.geogig.api.RevFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Retrieves the set of property descriptors for the given feature type.
 */
public class DescribeFeatureType extends AbstractGeoGigOp<ImmutableSet<PropertyDescriptor>> {

    private RevFeatureType featureType;

    /**
     * @param featureType the {@link RevFeatureType} to describe
     */
    public DescribeFeatureType setFeatureType(RevFeatureType featureType) {
        this.featureType = featureType;
        return this;
    }

    /**
     * Retrieves the set of property descriptors for the given feature type.
     * 
     * @return a sorted set of all the property descriptors of the feature type.
     */
    @Override
    protected  ImmutableSet<PropertyDescriptor> _call() {
        Preconditions.checkState(featureType != null, "FeatureType has not been set.");

        FeatureType type = featureType.type();

        ImmutableSet.Builder<PropertyDescriptor> propertySetBuilder = new ImmutableSet.Builder<PropertyDescriptor>();

        propertySetBuilder.addAll(type.getDescriptors());

        return propertySetBuilder.build();
    }
}
