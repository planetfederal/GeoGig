/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing;

import org.locationtech.geogig.api.AbstractGeoGitOp;
import org.locationtech.geogig.repository.Repository;

/**
 * Resolves the current repository
 * 
 */
public class ResolveRepository extends AbstractGeoGitOp<Repository> {

    @Override
    protected Repository _call() {
        return repository();
    }
}
