/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.bdbje;

import org.geogit.api.Platform;
import org.geogit.repository.Hints;
import org.geogit.repository.RepositoryConnectionException;
import org.geogit.storage.ConfigDatabase;
import org.geogit.storage.ObjectDatabase;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;

public final class JEStagingDatabase_v0_1 extends JEStagingDatabase {
    @Inject
    public JEStagingDatabase_v0_1(final ObjectDatabase repositoryDb,
            final EnvironmentBuilder envBuilder, final Platform platform,
            final ConfigDatabase configDB, final Hints hints) {
        super(repositoryDb, stagingDbSupplier(envBuilder, configDB, hints), platform, configDB);
    }

    private static Supplier<JEObjectDatabase> stagingDbSupplier(
            final EnvironmentBuilder envBuilder, final ConfigDatabase configDB, final Hints hints) {
        return Suppliers.memoize(new Supplier<JEObjectDatabase>() {
            @Override
            public JEObjectDatabase get() {
                boolean readOnly = hints.getBoolean(Hints.STAGING_READ_ONLY);
                envBuilder.setIsStagingDatabase(true);
                JEObjectDatabase db = new JEObjectDatabase_v0_1(configDB, envBuilder, readOnly,
                        JEStagingDatabase.ENVIRONMENT_NAME);
                return db;
            }
        });
    }

    @Override
    public void configure() throws RepositoryConnectionException {
        RepositoryConnectionException.StorageType.STAGING.configure(configDB, "bdbje", "0.1");
    }

    @Override
    public void checkConfig() throws RepositoryConnectionException {
        RepositoryConnectionException.StorageType.STAGING.verify(configDB, "bdbje", "0.1");
    }
}
