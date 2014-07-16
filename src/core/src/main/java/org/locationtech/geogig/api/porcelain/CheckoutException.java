/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

/**
 * Exception thrown by the {@link CheckoutOp checkout} op.
 * <p>
 * 
 * @TODO: define and codify the possible causes for a checkout to fail
 */
@SuppressWarnings("serial")
public class CheckoutException extends RuntimeException {

    public enum StatusCode {
        LOCAL_CHANGES_NOT_COMMITTED {
            public String message() {
                return "Doing a checkout without a clean working tree and index is currently unsupported.";
            }
        },
        UNMERGED_PATHS {
            public String message() {
                return "There are unmerged paths.";
            }
        };

        public abstract String message();
    }

    public StatusCode statusCode;

    public CheckoutException(String msg, StatusCode statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }

    public CheckoutException(StatusCode statusCode) {
        super(statusCode.message());
        this.statusCode = statusCode;
    }

}
