/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Provides a base interface for reading GeoGit objects from an {@link InputStream}.
 * 
 * @param <T> the type of the object to read
 */
public interface ObjectReader<T extends RevObject> {

    /**
     * Hint of type {@link GeometryFactory}
     */
    public static final String JTS_GEOMETRY_FACTORY = "JTS_GEOMETRY_FACTORY";

    /**
     * Hint of type Boolean
     */
    public static final String USE_PROVIDED_FID = "USE_PROVIDED_FID";

    /**
     * Reads an object from the given input stream and assigns it the provided {@link ObjectId id}.
     * 
     * @param id the id for the object to create
     * @param rawData the input stream of the object
     * @return the final object
     * @throws IllegalArgumentException if the provided stream does not represents an object of the
     *         required type
     */
    public T read(ObjectId id, InputStream rawData) throws IllegalArgumentException;

}
