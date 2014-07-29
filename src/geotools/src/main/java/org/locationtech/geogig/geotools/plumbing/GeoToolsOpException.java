/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.geotools.plumbing;

/**
 * Exception thrown by operations within the GeoTools extension.
 * 
 */
@SuppressWarnings("serial")
public class GeoToolsOpException extends RuntimeException {
    /**
     * Enumeration of possible status codes that indicate what type of exception occurred.
     */
    public enum StatusCode {
        ALL_AND_TABLE_DEFINED, DATASTORE_NOT_DEFINED, TABLE_NOT_DEFINED, NO_FEATURES_FOUND, TABLE_NOT_FOUND, UNABLE_TO_GET_NAMES, UNABLE_TO_GET_FEATURES, UNABLE_TO_INSERT, UNABLE_TO_ADD, CANNOT_CREATE_FEATURESTORE, ALTER_AND_ALL_DEFINED, MIXED_FEATURE_TYPES, INCOMPATIBLE_FEATURE_TYPE
    }

    /**
     * The status code for this exception.
     */
    public StatusCode statusCode;

    /**
     * Construct a new exception with the given status code.
     * 
     * @param statusCode the status code for this exception
     */
    public GeoToolsOpException(StatusCode statusCode) {
        this(null, statusCode);
    }

    /**
     * Construct a new exception with the given cause and status code.
     * 
     * @param e the cause of this exception
     * @param statusCode the status code for this exception
     */
    public GeoToolsOpException(Exception e, StatusCode statusCode) {
        super(e);
        this.statusCode = statusCode;
    }
}
