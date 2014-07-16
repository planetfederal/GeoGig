/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.geotools.cli.porcelain;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.geotools.data.spatialite.SpatiaLiteDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;
import org.locationtech.geogig.cli.AbstractCommand;
import org.locationtech.geogig.cli.CLICommand;
import org.locationtech.geogig.cli.CommandFailedException;

import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.internal.Maps;

/**
 * A template for SpatiaLite commands; provides out of the box support for the --help argument so
 * far.
 * 
 * @see CLICommand
 */
public abstract class AbstractSLCommand extends AbstractCommand implements CLICommand {

    /**
     * Common arguments for SpatiaLite commands.
     * 
     * @see SLCommonArgs
     */
    @ParametersDelegate
    public SLCommonArgs commonArgs = new SLCommonArgs();

    /**
     * Factory for constructing the data store.
     * 
     * @see SpatiaLiteDataStoreFactory
     */
    public AbstractDataStoreFactory dataStoreFactory = new SpatiaLiteDataStoreFactory();

    /**
     * Constructs a new SpatiaLite data store using connection parameters from {@link SLCommonArgs}.
     * 
     * @return the constructed data store
     * @throws CommandFailedException
     * @see DataStore
     */
    protected DataStore getDataStore() {
        Map<String, Serializable> params = Maps.newHashMap();
        params.put(SpatiaLiteDataStoreFactory.DBTYPE.key, "spatialite");
        params.put(SpatiaLiteDataStoreFactory.DATABASE.key, commonArgs.database);
        params.put(SpatiaLiteDataStoreFactory.USER.key, commonArgs.username);

        try {
            DataStore dataStore = dataStoreFactory.createDataStore(params);

            if (dataStore == null) {
                throw new CommandFailedException(
                        "Unable to connect using the specified database parameters.");
            }

            if (dataStore instanceof JDBCDataStore) {
                Connection con = null;
                con = ((JDBCDataStore) dataStore).getDataSource().getConnection();
                ((JDBCDataStore) dataStore).closeSafe(con);
            }
            return dataStore;
        } catch (IOException e) {
            throw new CommandFailedException(
                    "Unable to connect using the specified database parameters.", e);
        } catch (SQLException e) {
            throw new CommandFailedException("Unable to validate the database connection.", e);
        }
    }
}
