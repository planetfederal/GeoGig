/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Exception thrown by the push process.
 * 
 */
@SuppressWarnings("serial")
public class SynchronizationException extends RuntimeException {
    /**
     * Possible status codes for Push exceptions.
     */
    public enum StatusCode {
        NOTHING_TO_PUSH, REMOTE_HAS_CHANGES, HISTORY_TOO_SHALLOW, CANNOT_PUSH_TO_SYMBOLIC_REF
    }

    public StatusCode statusCode;

    /**
     * Constructs a new {@code PushException} with the given status code.
     * 
     * @param statusCode the status code for this exception
     */
    public SynchronizationException(StatusCode statusCode) {
        this(null, statusCode);
    }

    /**
     * Construct a new exception with the given cause and status code.
     * 
     * @param e the cause of this exception
     * @param statusCode the status code for this exception
     */
    public SynchronizationException(Exception e, StatusCode statusCode) {
        super(e);
        this.statusCode = statusCode;
    }
}
