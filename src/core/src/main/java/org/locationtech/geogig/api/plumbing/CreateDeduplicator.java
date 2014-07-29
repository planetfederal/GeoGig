/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing;

import org.locationtech.geogig.api.AbstractGeoGigOp;
import org.locationtech.geogig.storage.DeduplicationService;
import org.locationtech.geogig.storage.Deduplicator;

public class CreateDeduplicator extends AbstractGeoGigOp<Deduplicator> {

    @Override
    protected  Deduplicator _call() {
        DeduplicationService deduplicationService;
        deduplicationService = context.deduplicationService();
        return deduplicationService.createDeduplicator();
    }
}
