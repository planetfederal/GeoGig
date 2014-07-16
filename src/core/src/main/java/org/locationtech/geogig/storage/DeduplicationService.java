/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage;

import org.locationtech.geogig.di.Singleton;

/**
 * A service for providing deduplicators.
 */
@Singleton
public interface DeduplicationService {
    /**
     * Create a new Deduplicator.  Clients MUST ensure that the deduplicator's
     * release() method is called.  For example:
     *
     * <code>
     *   Deduplicator deduplicator = deduplicationService().createDeduplicator();
     *   try {
     *       client.use(deduplicator);
     *   } finally {
     *       deduplicator.release();
     *   }
     * </code>
     */
    Deduplicator createDeduplicator();
}
