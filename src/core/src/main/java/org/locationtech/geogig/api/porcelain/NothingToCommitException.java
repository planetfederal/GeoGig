/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Indicates there are no staged changes to commit as the result of the execution of a
 * {@link CommitOp}
 * 
 */
public class NothingToCommitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code NothingToCommitException} with the given message.
     * 
     * @param msg the message for the exception
     */
    public NothingToCommitException(String msg) {
        super(msg);
    }
}
