/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class StagingDbCompositionHelper {

    public static Iterator<RevObject> getAll(final ObjectDatabase objectDb,
            final ObjectDatabase stagingDb, final Iterable<ObjectId> ids,
            final BulkOpListener listener) {

        final List<ObjectId> missingInStaging = Lists.newLinkedList();

        final int limit = 10000;

        final BulkOpListener stagingListener = new BulkOpListener.ForwardingListener(listener) {
            @Override
            public void notFound(ObjectId id) {
                missingInStaging.add(id);
            }
        };

        final Iterator<RevObject> foundInStaging = stagingDb.getAll(ids, stagingListener);

        Iterator<RevObject> compositeIterator = new AbstractIterator<RevObject>() {

            Iterator<RevObject> forwardedToObjectDb = Iterators.emptyIterator();

            @Override
            protected RevObject computeNext() {
                if (forwardedToObjectDb.hasNext()) {
                    return forwardedToObjectDb.next();
                }
                if (missingInStaging.size() >= limit) {
                    List<ObjectId> missing = new ArrayList<ObjectId>(missingInStaging);
                    missingInStaging.clear();

                    forwardedToObjectDb = objectDb.getAll(missing, listener);
                    return computeNext();
                }
                if (foundInStaging.hasNext()) {
                    return foundInStaging.next();
                } else if (forwardedToObjectDb.hasNext()) {
                    return forwardedToObjectDb.next();
                } else if (!missingInStaging.isEmpty()) {
                    List<ObjectId> missing = new ArrayList<ObjectId>(missingInStaging);
                    missingInStaging.clear();
                    forwardedToObjectDb = objectDb.getAll(missing, listener);
                    return computeNext();
                }
                return endOfData();
            }
        };

        return compositeIterator;
    }
}
