/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.di;

import java.util.Map;

import org.locationtech.geogig.storage.ConfigDatabase;

import com.google.inject.Provider;

public abstract class FormatSelector<T> implements Provider<T> {
    private final ConfigDatabase config;

    private final Map<VersionedFormat, Provider<T>> plugins;

    public FormatSelector(ConfigDatabase config, Map<VersionedFormat, Provider<T>> plugins) {
        this.config = config;
        this.plugins = plugins;
    }

    protected abstract VersionedFormat readConfig(ConfigDatabase config);

    @Override
    final public T get() {
        try {
            VersionedFormat configuredFormat = readConfig(config);
            Provider<T> formatProvider = plugins.get(configuredFormat);
            if (formatProvider == null) {
                throw new RuntimeException("No such format: " + configuredFormat + "(from "
                        + config.getAll() + ")");
            } else {
                return formatProvider.get();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
