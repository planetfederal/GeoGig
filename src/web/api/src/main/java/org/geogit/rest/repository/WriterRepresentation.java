/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.rest.repository;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

/**
 *
 */
abstract class WriterRepresentation extends OutputRepresentation {

    public WriterRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        Writer writer = null;

        if (getCharacterSet() != null) {
            writer = new OutputStreamWriter(outputStream, getCharacterSet().getName());
        } else {
            // Use the default HTTP character set
            writer = new OutputStreamWriter(outputStream, CharacterSet.UTF_8.getName());
        }

        write(writer);
        writer.flush();
    }

    public abstract void write(Writer writer) throws IOException;

}
