/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.storage.bdbje;

import org.geogit.storage.DeduplicationService;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.StagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 *
 */
public class JEStorageModule extends AbstractModule {

    @Override
    protected void configure() {
        // BDB JE bindings for the different kinds of databases
        bind(ObjectDatabase.class).to(JEObjectDatabase_v0_1.class).in(Scopes.SINGLETON);
        bind(StagingDatabase.class).to(JEStagingDatabase_v0_1.class).in(Scopes.SINGLETON);
        bind(DeduplicationService.class).to(BDBJEDeduplicationService.class).in(Scopes.SINGLETON);
        bind(GraphDatabase.class).to(JEGraphDatabase_v0_1.class).in(Scopes.SINGLETON);

        // this module's specific. Used by the JE*Databases to set up the db environment
        // A new instance of each db
        bind(EnvironmentBuilder.class).in(Scopes.NO_SCOPE);
    }

}
