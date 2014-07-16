/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Exception thrown by remote commands.
 * 
 */
@SuppressWarnings("serial")
public class RemoteException extends RuntimeException {

    /**
     * Possible status codes for remote exceptions.
     */
    public enum StatusCode {
        REMOTE_NOT_FOUND, MISSING_NAME, MISSING_URL, REMOTE_ALREADY_EXISTS
    }

    public StatusCode statusCode;

    /**
     * Constructs a new {@code RemoteException} with the gien status code.
     * 
     * @param statusCode the status code for this exception
     */
    public RemoteException(StatusCode statusCode) {
        this(null, statusCode);
    }

    /**
     * Construct a new exception with the given cause and status code.
     * 
     * @param e the cause of this exception
     * @param statusCode the status code for this exception
     */
    public RemoteException(Exception e, StatusCode statusCode) {
        super(e);
        this.statusCode = statusCode;
    }
}
