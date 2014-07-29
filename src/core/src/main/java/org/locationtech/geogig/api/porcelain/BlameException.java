/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Exception thrown by BlameOp that contains the error status code.
 * 
 */
@SuppressWarnings("serial")
public class BlameException extends RuntimeException {
    /**
     * Possible status codes for Blame exceptions.
     */
    public enum StatusCode {
        FEATURE_NOT_FOUND, PATH_NOT_FEATURE
    }

    public StatusCode statusCode;

    /**
     * Constructs a new {@code BlameException} with the given status code.
     * 
     * @param statusCode the status code for this exception
     */
    public BlameException(StatusCode statusCode) {
        this(null, statusCode);
    }

    /**
     * Construct a new exception with the given cause and status code.
     * 
     * @param e the cause of this exception
     * @param statusCode the status code for this exception
     */
    public BlameException(Exception e, StatusCode statusCode) {
        super(e);
        this.statusCode = statusCode;
    }
}
