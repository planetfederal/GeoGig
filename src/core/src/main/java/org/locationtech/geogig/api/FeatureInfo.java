/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api;

import org.opengis.feature.Feature;

/**
 * A class to compactly store information about a feature, including its path and feature type. This
 * is to be used in the context of applying patches or performing a merge operation, where this type
 * of information is needed.
 * 
 */
public class FeatureInfo {

    private Feature feature;

    private RevFeatureType featureType;

    private String path;

    public FeatureInfo(Feature feature, RevFeatureType featureType, String path) {
        this.path = path;
        this.feature = feature;
        this.featureType = featureType;
    }

    /**
     * The feature
     * 
     * @return
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * The feature type of the feature
     * 
     * @return
     */
    public RevFeatureType getFeatureType() {
        return featureType;
    }

    /**
     * The path to where the feature is to be added
     * 
     * @return
     */
    public String getPath() {
        return path;
    }

}