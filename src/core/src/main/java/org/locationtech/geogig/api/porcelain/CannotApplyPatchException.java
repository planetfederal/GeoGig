/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

import org.locationtech.geogig.api.plumbing.diff.Patch;

/**
 * This exception indicate that a given patch is outdated and does not correspond to the current
 * state of the working tree, so it cannot be applied.
 * 
 */
public class CannotApplyPatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * A patch with the conflicting changes of the patch that caused the exception
     * 
     */
    private Patch patch;

    public CannotApplyPatchException(Patch patch) {
        super("Error: Patch cannot be applied\n\nConflicting entries:\n\n" + patch.toString());
        this.patch = patch;
    }

    public Patch getPatch() {
        return patch;
    }

}
