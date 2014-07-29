/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import java.util.List;

import org.geogit.api.plumbing.merge.Conflict;

import com.google.common.base.Optional;
import com.google.inject.Provider;

public class ForwardingStagingDatabase extends ForwardingObjectDatabase implements StagingDatabase {

    public ForwardingStagingDatabase(Provider<StagingDatabase> subject) {
        super(subject);
    }

    @Override
    public Optional<Conflict> getConflict(String namespace, String path) {
        return ((StagingDatabase) subject.get()).getConflict(namespace, path);
    }

    @Override
    public List<Conflict> getConflicts(String namespace, String pathFilter) {
        return ((StagingDatabase) subject.get()).getConflicts(namespace, pathFilter);
    }

    @Override
    public void addConflict(String namespace, Conflict conflict) {
        ((StagingDatabase) subject.get()).addConflict(namespace, conflict);
    }

    @Override
    public void removeConflict(String namespace, String path) {
        ((StagingDatabase) subject.get()).removeConflict(namespace, path);
    }

    @Override
    public void removeConflicts(String namespace) {
        ((StagingDatabase) subject.get()).removeConflicts(namespace);
    }

    @Override
    public boolean hasConflicts(String namespace) {
        return ((StagingDatabase) subject.get()).hasConflicts(namespace);
    }

}
