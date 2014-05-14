/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.di;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.geogit.api.DefaultPlatform;
import org.geogit.api.Context;
import org.geogit.api.Platform;
import org.geogit.api.hooks.CommandHooksDecorator;
import org.geogit.api.hooks.Hookables;
import org.geogit.repository.Index;
import org.geogit.repository.Repository;
import org.geogit.repository.StagingArea;
import org.geogit.repository.WorkingTree;
import org.geogit.storage.ConfigDatabase;
import org.geogit.storage.DeduplicationService;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.ObjectSerializingFactory;
import org.geogit.storage.RefDatabase;
import org.geogit.storage.StagingDatabase;
import org.geogit.storage.datastream.DataStreamSerializationFactory;
import org.geogit.storage.fs.FileObjectDatabase;
import org.geogit.storage.fs.FileRefDatabase;
import org.geogit.storage.fs.IniFileConfigDatabase;
import org.geogit.storage.memory.HeapDeduplicationService;
import org.geogit.storage.memory.HeapGraphDatabase;
import org.geogit.storage.memory.HeapStagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

/**
 * Provides bindings for GeoGit singletons.
 * 
 * @see Context
 * @see Platform
 * @see Repository
 * @see ConfigDatabase
 * @see StagingArea
 * @see WorkingTree
 * @see ObjectDatabase
 * @see StagingDatabase
 * @see RefDatabase
 * @see GraphDatabase
 * @see ObjectSerializingFactory
 * @see DeduplicationService
 */

public class GeogitModule extends AbstractModule {

    /**
     * 
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {

        Provider<ExecutorService> fineGrainedExecutor = new Provider<ExecutorService>() {
            @Override
            public ExecutorService get() {
                int availableProcessors = Runtime.getRuntime().availableProcessors();
                return Executors.newFixedThreadPool(availableProcessors);
            }
        };

        bind(ExecutorService.class).toProvider(fineGrainedExecutor).in(Scopes.SINGLETON);

        bind(Context.class).to(GuiceInjector.class).in(Scopes.SINGLETON);

        Multibinder.newSetBinder(binder(), Decorator.class);
        bind(DecoratorProvider.class).in(Scopes.SINGLETON);

        bind(Platform.class).to(DefaultPlatform.class).asEagerSingleton();

        bind(Repository.class).in(Scopes.SINGLETON);
        bind(ConfigDatabase.class).to(IniFileConfigDatabase.class).in(Scopes.SINGLETON);
        bind(StagingArea.class).to(Index.class).in(Scopes.SINGLETON);
        bind(StagingDatabase.class).to(HeapStagingDatabase.class).in(Scopes.SINGLETON);
        bind(WorkingTree.class).in(Scopes.SINGLETON);
        bind(GraphDatabase.class).to(HeapGraphDatabase.class).in(Scopes.SINGLETON);

        bind(ObjectDatabase.class).to(FileObjectDatabase.class).in(Scopes.SINGLETON);
        bind(RefDatabase.class).to(FileRefDatabase.class).in(Scopes.SINGLETON);

        bind(ObjectSerializingFactory.class).to(DataStreamSerializationFactory.class).in(
                Scopes.SINGLETON);

        bind(DeduplicationService.class).to(HeapDeduplicationService.class).in(Scopes.SINGLETON);

        bindCommitGraphInterceptor();

        bindConflictCheckingInterceptor();

        bindDecorator(binder(), new CommandHooksDecorator());
    }

    private void bindConflictCheckingInterceptor() {
        bindDecorator(binder(), new ConflictInterceptor());
    }

    private void bindCommitGraphInterceptor() {

        ObjectDatabasePutInterceptor commitGraphUpdater = new ObjectDatabasePutInterceptor(
                getProvider(GraphDatabase.class));

        bindDecorator(binder(), commitGraphUpdater);
    }

    public static void bindDecorator(Binder binder, Decorator decorator) {

        Multibinder.newSetBinder(binder, Decorator.class).addBinding().toInstance(decorator);

    }
}
