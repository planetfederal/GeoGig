/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.web.api;

/**
 * A user-input (or lack thereof) driven exception. Purposefully does not have a constructor to
 * allow a Throwable cause to be specified.
 */
@SuppressWarnings("serial")
public class CommandSpecException extends IllegalArgumentException {

    /**
     * Constructs a new {code CommandSpecException} with the given message.
     * 
     * @param message the message
     */
    public CommandSpecException(String message) {
        super(message);
    }

}
