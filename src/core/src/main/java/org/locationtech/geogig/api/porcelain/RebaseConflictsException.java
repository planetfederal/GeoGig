/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Exception that indicates that a rebase operation cannot be finished due to merge conflicts
 */
public class RebaseConflictsException extends ConflictsException {

    private static final long serialVersionUID = 1L;

    public RebaseConflictsException(String msg) {
        super(msg);
    }

}
