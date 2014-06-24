/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import java.io.Serializable;
import java.util.Map;

import org.geogit.api.RevCommit;
import org.geogit.api.RevFeature;
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;

/**
 * The ObjectSerializingFactory is used to create instances of the various writers and readers used
 * to work with the serialized forms of various repository elements.
 * 
 */
public interface ObjectSerializingFactory {

    /**
     * Creates an instance of a commit reader.
     * 
     * @return commit reader
     */

    public ObjectReader<RevCommit> createCommitReader();

    /**
     * Creates an instance of a RevTree reader.
     */
    public ObjectReader<RevTree> createRevTreeReader();

    /**
     * Creates an instance of a Feature reader that can parse features.
     * 
     * @return feature reader
     */
    public ObjectReader<RevFeature> createFeatureReader();

    /**
     * Creates an instance of a Feature reader that can parse features.
     * 
     * @param hints feature creation hints
     * @return feature reader
     */
    public ObjectReader<RevFeature> createFeatureReader(final Map<String, Serializable> hints);

    /**
     * Creates an instance of a feature type reader that can parse feature types.
     * 
     * @return feature type reader
     */
    public ObjectReader<RevFeatureType> createFeatureTypeReader();

    public <T extends RevObject> ObjectWriter<T> createObjectWriter(TYPE type);

    /**
     * @param type
     * @return
     */
    public <T extends RevObject> ObjectReader<T> createObjectReader(TYPE type);

    public ObjectReader<RevObject> createObjectReader();
}
