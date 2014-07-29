/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.rest.repository;

import javax.annotation.Nullable;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 * An exception that specifies the Restlet representation and status code that should be used to
 * report as an HTTP response.
 */
public class RestletException extends RuntimeException {

    private static final long serialVersionUID = -7081583295790313316L;

    private final Status status;

    private final Representation outputRepresentation;

    /**
     * @param message The message to report this error to the user (will report MIME Type as
     *        {@code text/plain})
     * @param status the HTTP status to report
     */
    public RestletException(String message, Status status) {
        this(message, status, null);
    }

    /**
     * @param message The message to report this error to the user (will report MIME Type as
     *        {@code text/plain})
     * @param status the HTTP status to report
     * @param cause the cause of the exception
     */
    public RestletException(String message, Status status, @Nullable Throwable cause) {
        super(cause);
        this.outputRepresentation = new StringRepresentation(message
                + (cause == null ? "" : (":" + cause.getMessage())), MediaType.TEXT_PLAIN);
        this.status = status;
    }

    /**
     * @return the HTTP status code to report
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return the output representation for this exception
     */
    public Representation getRepresentation() {
        return outputRepresentation;
    }
}
