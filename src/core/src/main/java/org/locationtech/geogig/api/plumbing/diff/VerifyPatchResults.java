/* Copyright (c) 2013 OpenPlans. All rights reserved. 
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing.diff;


/**
 * A class to contains the results of a verify patch operation. It contains two patches, one with
 * the changes that can be applied on the current working tree, and another one with the changes
 * that cannot be applied
 * 
 */
public class VerifyPatchResults {

    private Patch toApply;

    private Patch toReject;

    public Patch getToApply() {
        return toApply;
    }

    /**
     * Returns the patch with the changes to reject
     * 
     * @return
     */
    public Patch getToReject() {
        return toReject;
    }

    /**
     * Returns the patch with the changes to apply
     * 
     * @return
     */
    public VerifyPatchResults(Patch toApply, Patch toReject) {
        this.toApply = toApply;
        this.toReject = toReject;

    }
}
