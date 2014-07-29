/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Exception that indicates that an operation cannot be finished due to conflicts
 */
public class ConflictsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictsException(String msg) {
        super(msg);
    }

}
