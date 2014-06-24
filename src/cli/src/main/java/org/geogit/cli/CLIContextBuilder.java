/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.cli;

import org.geogit.api.Context;
import org.geogit.api.ContextBuilder;
import org.geogit.di.GeogitModule;
import org.geogit.di.PluginDefaults;
import org.geogit.di.PluginsModule;
import org.geogit.di.VersionedFormat;
import org.geogit.di.caching.CachingModule;
import org.geogit.metrics.MetricsModule;
import org.geogit.repository.Hints;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.RefDatabase;
import org.geogit.storage.StagingDatabase;
import org.geogit.storage.bdbje.JEGraphDatabase_v0_1;
import org.geogit.storage.bdbje.JEGraphDatabase_v0_2;
import org.geogit.storage.bdbje.JEObjectDatabase_v0_1;
import org.geogit.storage.bdbje.JEObjectDatabase_v0_2;
import org.geogit.storage.bdbje.JEStagingDatabase_v0_1;
import org.geogit.storage.bdbje.JEStagingDatabase_v0_2;
import org.geogit.storage.fs.FileRefDatabase;
import org.geogit.storage.mongo.MongoGraphDatabase;
import org.geogit.storage.mongo.MongoObjectDatabase;
import org.geogit.storage.mongo.MongoStagingDatabase;
import org.geogit.storage.sqlite.SQLiteStorage;
import org.geogit.storage.sqlite.XerialGraphDatabase;
import org.geogit.storage.sqlite.XerialObjectDatabase;
import org.geogit.storage.sqlite.XerialStagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.util.Modules;

public class CLIContextBuilder extends ContextBuilder {

    private static final VersionedFormat DEFAULT_REFS = new VersionedFormat("file", "1.0");

    private static final VersionedFormat DEFAULT_OBJECTS = new VersionedFormat("bdbje", "0.2");

    private static final VersionedFormat DEFAULT_STAGING = new VersionedFormat("bdbje", "0.2");

    private static final VersionedFormat DEFAULT_GRAPH = new VersionedFormat("bdbje", "0.2");

    private static final PluginDefaults defaults = new PluginDefaults(DEFAULT_OBJECTS,//
            DEFAULT_STAGING,//
            DEFAULT_REFS,//
            DEFAULT_GRAPH);

    @Override
    public Context build(Hints hints) {
        return Guice.createInjector(
                Modules.override(new GeogitModule(), new CachingModule()).with(new MetricsModule(),
                        new PluginsModule(), new DefaultPlugins(), new HintsModule(hints)))
                .getInstance(org.geogit.api.Context.class);
    }

    public static class DefaultPlugins extends AbstractModule {
        @Override
        protected void configure() {
            bind(PluginDefaults.class).toInstance(defaults);
            MapBinder<VersionedFormat, RefDatabase> refPlugins = MapBinder.newMapBinder(binder(),
                    VersionedFormat.class, RefDatabase.class);
            refPlugins //
                    .addBinding(DEFAULT_REFS)//
                    .to(FileRefDatabase.class)//
                    .in(Scopes.SINGLETON);
            MapBinder<VersionedFormat, ObjectDatabase> objectPlugins = MapBinder.newMapBinder(
                    binder(), VersionedFormat.class, ObjectDatabase.class);
            objectPlugins //
                    .addBinding(new VersionedFormat("bdbje", "0.2"))//
                    .to(JEObjectDatabase_v0_2.class)//
                    .in(Scopes.SINGLETON);//
            objectPlugins //
                    .addBinding(new VersionedFormat("bdbje", "0.1"))//
                    .to(JEObjectDatabase_v0_1.class)//
                    .in(Scopes.SINGLETON);
            objectPlugins //
                    .addBinding(new VersionedFormat("mongodb", "0.1"))//
                    .to(MongoObjectDatabase.class)//
                    .in(Scopes.SINGLETON);
            objectPlugins //
                    .addBinding(
                            new VersionedFormat(SQLiteStorage.FORMAT_NAME, SQLiteStorage.VERSION))//
                    .to(XerialObjectDatabase.class)//
                    .in(Scopes.SINGLETON);
            MapBinder<VersionedFormat, StagingDatabase> stagingPlugins = MapBinder.newMapBinder(
                    binder(), VersionedFormat.class, StagingDatabase.class);
            stagingPlugins //
                    .addBinding(new VersionedFormat("mongodb", "0.1"))//
                    .to(MongoStagingDatabase.class)//
                    .in(Scopes.SINGLETON);
            stagingPlugins //
                    .addBinding(new VersionedFormat("bdbje", "0.2"))//
                    .to(JEStagingDatabase_v0_2.class)//
                    .in(Scopes.SINGLETON);
            stagingPlugins //
                    .addBinding(new VersionedFormat("bdbje", "0.1"))//
                    .to(JEStagingDatabase_v0_1.class)//
                    .in(Scopes.SINGLETON);
            stagingPlugins //
                    .addBinding(
                            new VersionedFormat(SQLiteStorage.FORMAT_NAME, SQLiteStorage.VERSION))//
                    .to(XerialStagingDatabase.class)//
                    .in(Scopes.SINGLETON);
            MapBinder<VersionedFormat, GraphDatabase> graphPlugins = MapBinder.newMapBinder(
                    binder(), VersionedFormat.class, GraphDatabase.class);
            graphPlugins //
                    .addBinding(new VersionedFormat("bdbje", "0.2")) //
                    .to(JEGraphDatabase_v0_2.class) //
                    .in(Scopes.SINGLETON);
            graphPlugins //
                    .addBinding(new VersionedFormat("bdbje", "0.1")) //
                    .to(JEGraphDatabase_v0_1.class) //
                    .in(Scopes.SINGLETON);
            graphPlugins //
                    .addBinding(new VersionedFormat("mongodb", "0.1")) //
                    .to(MongoGraphDatabase.class) //
                    .in(Scopes.SINGLETON);
            graphPlugins //
                    .addBinding(
                            new VersionedFormat(SQLiteStorage.FORMAT_NAME, SQLiteStorage.VERSION)) //
                    .to(XerialGraphDatabase.class) //
                    .in(Scopes.SINGLETON);
        }
    }
}
