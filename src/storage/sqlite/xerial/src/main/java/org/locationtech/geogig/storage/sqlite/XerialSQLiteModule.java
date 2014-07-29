/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.sqlite;

import org.locationtech.geogig.storage.ConfigDatabase;
import org.locationtech.geogig.storage.GraphDatabase;
import org.locationtech.geogig.storage.ObjectDatabase;
import org.locationtech.geogig.storage.StagingDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module for the Xerial SQLite storage backend.
 * <p>
 * More information about the SQLite jdbc driver available at {@link https
 * ://bitbucket.org/xerial/sqlite-jdbc}.
 * </p>
 * 
 * @author Justin Deoliveira, Boundless
 */
public class XerialSQLiteModule extends AbstractModule {

    static Logger LOG = LoggerFactory.getLogger(XerialSQLiteModule.class);

    @Override
    protected void configure() {
        bind(ConfigDatabase.class).to(XerialConfigDatabase.class).in(Scopes.SINGLETON);
        bind(ObjectDatabase.class).to(XerialObjectDatabase.class).in(Scopes.SINGLETON);
        bind(GraphDatabase.class).to(XerialGraphDatabase.class).in(Scopes.SINGLETON);
        bind(StagingDatabase.class).to(XerialStagingDatabase.class).in(Scopes.SINGLETON);
    }

}
