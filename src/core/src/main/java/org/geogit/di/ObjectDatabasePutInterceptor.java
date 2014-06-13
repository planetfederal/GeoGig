/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.di;

import java.util.Iterator;

import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.api.RevObject;
import org.geogit.storage.BulkOpListener;
import org.geogit.storage.ForwardingObjectDatabase;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.StagingDatabase;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.inject.Provider;
import com.google.inject.util.Providers;

/**
 * Method interceptor for {@link ObjectDatabase#put(RevObject)} that adds new commits to the graph
 * database.
 */
class ObjectDatabasePutInterceptor implements Decorator {

    private Provider<GraphDatabase> graphDb;

    public ObjectDatabasePutInterceptor(Provider<GraphDatabase> graphDb) {
        this.graphDb = graphDb;
    }

    @Override
    public boolean canDecorate(Object subject) {
        boolean canDecorate = subject instanceof ObjectDatabase
                && !(subject instanceof StagingDatabase);
        return canDecorate;
    }

    @Override
    public ObjectDatabase decorate(Object subject) {
        return new GraphUpdatingObjectDatabase(graphDb, (ObjectDatabase) subject);
    }

    private static class GraphUpdatingObjectDatabase extends ForwardingObjectDatabase {

        private Provider<GraphDatabase> graphDb;

        public GraphUpdatingObjectDatabase(Provider<GraphDatabase> graphDb, ObjectDatabase subject) {
            super(Providers.of(subject));
            this.graphDb = graphDb;
        }

        @Override
        public boolean put(RevObject object) {

            final boolean inserted = super.put(object);

            if (inserted && RevObject.TYPE.COMMIT.equals(object.getType())) {
                RevCommit commit = (RevCommit) object;
                graphDb.get().put(commit.getId(), commit.getParentIds());
            }
            return inserted;
        }

        @Override
        public void putAll(Iterator<? extends RevObject> objects) {
            putAll(objects, BulkOpListener.NOOP_LISTENER);
        }

        @Override
        public void putAll(Iterator<? extends RevObject> objects, BulkOpListener listener) {

            //final List<RevCommit> addedCommits = Lists.newLinkedList();

            final Iterator<? extends RevObject> collectingIterator = Iterators.transform(objects,
                    new Function<RevObject, RevObject>() {

                        private final GraphDatabase graphDatabase = graphDb.get();

                        @Override
                        public RevObject apply(RevObject input) {
                            if (input instanceof RevCommit) {
                                RevCommit commit = (RevCommit) input;
                                ObjectId commitId = commit.getId();
                                ImmutableList<ObjectId> parentIds = commit.getParentIds();
                                graphDatabase.put(commitId, parentIds);

                                // addedCommits.add((RevCommit) input);
                            }
                            return input;
                        }
                    });

            super.putAll(collectingIterator, listener);

//            if (!addedCommits.isEmpty()) {
//                GraphDatabase graphDatabase = graphDb.get();
//                for (RevCommit commit : addedCommits) {
//                    ObjectId commitId = commit.getId();
//                    ImmutableList<ObjectId> parentIds = commit.getParentIds();
//                    graphDatabase.put(commitId, parentIds);
//                }
//            }
        }

    }

}
