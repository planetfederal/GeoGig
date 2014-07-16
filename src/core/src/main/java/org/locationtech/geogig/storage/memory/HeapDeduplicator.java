/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.memory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.storage.Deduplicator;

public class HeapDeduplicator implements Deduplicator {
    private Set<ObjectId> seen = new HashSet<ObjectId>();
    
    @Override
    public boolean visit(ObjectId id) {
        return !seen.add(id);
    }
    
    @Override
    public boolean isDuplicate(ObjectId id) {
        return seen.contains(id);
    }

    @Override
    public void removeDuplicates(List<ObjectId> ids) {
        ids.removeAll(seen);
    }
    
    @Override
    public void reset() {
    	seen.clear();
    }

    @Override
    public void release() {
    	seen = null;
    }
}
