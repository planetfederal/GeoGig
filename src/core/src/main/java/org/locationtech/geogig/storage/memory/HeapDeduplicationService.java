/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.memory;

import org.locationtech.geogig.storage.DeduplicationService;
import org.locationtech.geogig.storage.Deduplicator;

public class HeapDeduplicationService implements DeduplicationService {
    @Override
    public Deduplicator createDeduplicator() {
        return new HeapDeduplicator();
    }
}
