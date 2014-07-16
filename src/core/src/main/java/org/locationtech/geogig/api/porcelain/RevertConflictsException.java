/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Exception that indicates that a revert operation cannot be finished due to merge conflicts
 */
public class RevertConflictsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RevertConflictsException(String msg) {
        super(msg);
    }

}
