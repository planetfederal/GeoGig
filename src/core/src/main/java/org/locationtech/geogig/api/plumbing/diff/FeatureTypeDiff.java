/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing.diff;

import org.locationtech.geogig.api.ObjectId;

/**
 * Defines the differences between 2 versions of the a given feature type
 * 
 */
public class FeatureTypeDiff {

    private String path;

    private ObjectId newFeatureType;

    private ObjectId oldFeatureType;

    public FeatureTypeDiff(String path, ObjectId oldFeatureType, ObjectId newFeatureType) {
        this.path = path;
        this.newFeatureType = newFeatureType == null ? ObjectId.NULL : newFeatureType;
        this.oldFeatureType = oldFeatureType == null ? ObjectId.NULL : oldFeatureType;
    }

    /**
     * The Id of the new version of the feature type
     * 
     * @return
     */
    public ObjectId getNewFeatureType() {
        return newFeatureType;
    }

    /**
     * The Id of the old version of the feature type
     * 
     * @return
     */
    public ObjectId getOldFeatureType() {
        return oldFeatureType;
    }

    /**
     * The feature type path
     * 
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the reversed version of this difference
     * 
     * @return
     */
    public FeatureTypeDiff reversed() {
        return new FeatureTypeDiff(path, newFeatureType, oldFeatureType);
    }

    public String toString() {
        return path + "\t" + oldFeatureType.toString() + "\t" + newFeatureType.toString();
    }

}