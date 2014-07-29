/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.hooks;

public class CannotRunGeogitOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CannotRunGeogitOperationException() {
        // default constructor, needed by jdk6
    }

    /**
     * Constructs a new {@code CannotRunGeogitOperationException} with the given message.
     * 
     * @param msg the message for the exception
     */
    public CannotRunGeogitOperationException(String msg) {
        super(msg);
    }

    public CannotRunGeogitOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
