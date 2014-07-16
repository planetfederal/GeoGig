/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.metrics;

import org.locationtech.geogig.di.Decorator;
import org.locationtech.geogig.repository.Repository;
import org.locationtech.geogig.repository.Repository.RepositoryListener;

/**
 * Shuts down the metrics service at repository close() event
 */
class RepositoryDecorator implements Decorator {

    private HeapMemoryMetricsService service;

    private RepositoryListener listener;

    public RepositoryDecorator(HeapMemoryMetricsService service) {
        this.service = service;
    }

    @Override
    public boolean canDecorate(Object instance) {
        return instance instanceof Repository;
    }

    @Override
    public <I> I decorate(I subject) {
        if (listener == null) {
            listener = new RepositoryListener() {

                @Override
                public void opened(Repository repo) {
                    service.startAsync().awaitRunning();
                }

                @Override
                public void closed() {
                    service.stopAsync();
                }
            };
            ((Repository) subject).addListener(listener);
        }
        return subject;
    }
}
