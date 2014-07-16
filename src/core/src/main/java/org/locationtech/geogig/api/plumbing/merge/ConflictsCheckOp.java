/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing.merge;

import java.net.URL;

import org.locationtech.geogig.api.AbstractGeoGigOp;
import org.locationtech.geogig.api.plumbing.ResolveGeogigDir;

import com.google.common.base.Optional;

public class ConflictsCheckOp extends AbstractGeoGigOp<Boolean> {
    @Override
    protected  Boolean _call() {
        final Optional<URL> repoUrl = command(ResolveGeogigDir.class).call();
        Boolean hasConflicts = Boolean.FALSE;

        if (repoUrl.isPresent()) {
            boolean conflicts = stagingDatabase().hasConflicts(null);
            hasConflicts = Boolean.valueOf(conflicts);
        }
        return hasConflicts;
    }
}
