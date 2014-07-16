/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing.diff;

/**
 * A class with the counts of changed elements between two commits, divided in trees and features
 * 
 */
public class DiffObjectCount {

    private long featuresAdded, featuresRemoved, featuresChanged;

    private int treesAdded, treesRemoved, treesChanged;

    /**
     * Returns the total count of modified elements (i.e. sum of added, changed, and removed trees
     * and features)
     */
    public long count() {
        return featureCount() + treeCount();
    }

    /**
     * Returns the sum of added, modified, and removed features
     */
    public long featureCount() {
        return featuresAdded + featuresChanged + featuresRemoved;
    }

    /**
     * Returns the sum of added, modified, and removed trees
     */
    public int treeCount() {
        return treesAdded + treesChanged + treesRemoved;
    }

    /**
     * Increases the number of added features by a given number
     */
    void addedFeatures(long count) {
        featuresAdded += count;
    }

    /**
     * Increases the number of removed features by a given number
     */
    void removedFeatures(long count) {
        featuresRemoved += count;
    }

    /**
     * Increases the number of changed features by a given number
     */
    void changedFeatures(long count) {
        featuresChanged += count;
    }

    /**
     * Increases the number of added trees by a given number
     */
    void addedTrees(int count) {
        treesAdded += count;
    }

    /**
     * Increases the number of removed trees by a given number
     */
    void removedTrees(int count) {
        treesRemoved += count;
    }

    /**
     * Increases the number of changed trees by a given number
     */
    void changedTrees(int count) {
        treesChanged += count;
    }

    public long getFeaturesAdded() {
        return featuresAdded;
    }

    public long getFeaturesRemoved() {
        return featuresRemoved;
    }

    public long getFeaturesChanged() {
        return featuresChanged;
    }

    public int getTreesAdded() {
        return treesAdded;
    }

    public int getTreesRemoved() {
        return treesRemoved;
    }

    public int getTreesChanged() {
        return treesChanged;
    }

}
