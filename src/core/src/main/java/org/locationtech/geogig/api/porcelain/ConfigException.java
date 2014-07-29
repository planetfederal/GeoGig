/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Exception thrown by ConfigDatabase that contains the error status code.
 * 
 */
@SuppressWarnings("serial")
public class ConfigException extends RuntimeException {
    /**
     * Possible status codes for Config exceptions.
     */
    public enum StatusCode {
        INVALID_LOCATION, CANNOT_WRITE, SECTION_OR_NAME_NOT_PROVIDED, SECTION_OR_KEY_INVALID, OPTION_DOES_NOT_EXIST, MULTIPLE_OPTIONS_MATCH, INVALID_REGEXP, USERHOME_NOT_SET, TOO_MANY_ACTIONS, MISSING_SECTION, TOO_MANY_ARGS
    }

    public StatusCode statusCode;

    /**
     * Constructs a new {@code ConfigException} with the given status code.
     * 
     * @param statusCode the status code for this exception
     */
    public ConfigException(StatusCode statusCode) {
        this(null, statusCode);
    }

    /**
     * Construct a new exception with the given cause and status code.
     * 
     * @param e the cause of this exception
     * @param statusCode the status code for this exception
     */
    public ConfigException(Exception e, StatusCode statusCode) {
        super(e);
        this.statusCode = statusCode;
    }
}
