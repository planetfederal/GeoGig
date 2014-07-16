/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage;

import java.io.IOException;
import java.io.OutputStream;

import org.locationtech.geogig.api.RevObject;

/**
 * Provides an interface for writing objects to a given output stream.
 */
public interface ObjectWriter<T extends RevObject> {

    /**
     * Writes the object to the given output stream. Does not close the output stream, as it doesn't
     * belong to this object. The calling code is responsible of the outputstream life cycle.
     * 
     * @param object the object to serialize
     * @param out the stream to write to
     * @throws IOException
     */
    public void write(T object, OutputStream out) throws IOException;
}
