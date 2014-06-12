/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.osm.internal;

import static org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor.convertToDouble;
import static org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor.convertToFixed;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.geogit.api.Platform;
import org.geogit.storage.bdbje.EnvironmentBuilder;
import org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.CacheMode;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * A {@code PointCache} backed by a temporary BDB JE database, where each point is mapped as a
 * {@code long} key, and {@code int, int} ordinates, using the conversion from double precision to
 * integer defined in the osmosis {@link FixedPrecisionCoordinateConvertor} class.
 */
public class BDBJEPointCache implements PointCache {

    private static final Random random = new Random();

    private Environment environment;

    private Database database;

    public BDBJEPointCache(Platform platform) {
        String envName = "tmpPointCache_" + Math.abs(random.nextInt());

        EnvironmentConfig envCfg;
        envCfg = new EnvironmentConfig();
        envCfg.setAllowCreate(true);
        envCfg.setTransactional(false);

        envCfg.setSharedCache(true);
        envCfg.setCacheMode(CacheMode.MAKE_COLD);

        envCfg.setDurability(Durability.COMMIT_NO_SYNC);
        envCfg.setConfigParam(EnvironmentConfig.LOG_FILE_MAX, String.valueOf(1024 * 1024 * 1024));
        envCfg.setConfigParam(EnvironmentConfig.ENV_RUN_CLEANER, "false");
        envCfg.setConfigParam(EnvironmentConfig.ENV_RUN_CHECKPOINTER, "false");
        envCfg.setConfigParam("je.evictor.lruOnly", "false");
        envCfg.setConfigParam("je.evictor.nodesPerScan", "1000");

        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder(platform);
        environmentBuilder.setRelativePath("osm", envName);
        environmentBuilder.setIsStagingDatabase(true);
        environmentBuilder.setConfig(envCfg);

        this.environment = environmentBuilder.get();

        DatabaseConfig dbc = new DatabaseConfig();
        dbc.setAllowCreate(true);
        dbc.setTemporary(true);
        this.database = this.environment.openDatabase(null, "pointcache", dbc);
    }

    @Override
    public void put(Long nodeId, Coordinate coord) {

        DatabaseEntry key = new DatabaseEntry();
        LongBinding.longToEntry(nodeId.longValue(), key);

        DatabaseEntry data = CoordinateBinding.objectToEntry(coord);

        database.put(null, key, data);
    }

    @Override
    @Nullable
    public Coordinate get(long nodeId) {

        DatabaseEntry key = new DatabaseEntry();
        LongBinding.longToEntry(nodeId, key);

        DatabaseEntry data = new DatabaseEntry();
        OperationStatus status = database.get(null, key, data, LockMode.DEFAULT);
        if (OperationStatus.SUCCESS.equals(status)) {
            Coordinate coord = CoordinateBinding.entryToCoord(data);
            return coord;
        }
        throw new IllegalArgumentException(String.format("Node id %d not found", nodeId));
    }

    @Override
    public synchronized void dispose() {
        if (environment == null) {
            return;
        }
        final File envHome = environment.getHome();
        try {
            database.close();
        } catch (RuntimeException e) {
            throw new RuntimeException("Error closing point cache", e);
        } finally {
            database = null;
            try {
                environment.close();
            } finally {
                environment = null;
                envHome.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.delete();
                    }
                });
                envHome.delete();
            }
        }
    }

    /**
     * Tuple binding for OSM coordinates that stores ordinates as {@code int} as per
     * {@link FixedPrecisionCoordinateConvertor}
     */
    private static final class CoordinateBinding extends TupleBinding<Coordinate> {

        private static final CoordinateBinding INSTANCE = new CoordinateBinding();

        public static final DatabaseEntry objectToEntry(Coordinate coord) {
            DatabaseEntry data = new DatabaseEntry();
            INSTANCE.objectToEntry(coord, data);
            return data;
        }

        public static Coordinate entryToCoord(DatabaseEntry data) {
            return INSTANCE.entryToObject(data);
        }

        @Override
        public Coordinate entryToObject(TupleInput input) {
            int x = input.readInt();
            int y = input.readInt();
            return new Coordinate(convertToDouble(x), convertToDouble(y));
        }

        @Override
        public void objectToEntry(Coordinate c, TupleOutput output) {
            output.writeInt(convertToFixed(c.x));
            output.writeInt(convertToFixed(c.y));
        }

    }

    @Override
    public Coordinate[] get(List<Long> ids) {

        Coordinate[] coords = new Coordinate[ids.size()];

        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry data = new DatabaseEntry();

        for (int index = 0; index < ids.size(); index++) {
            Long nodeID = ids.get(index);
            LongBinding.longToEntry(nodeID.longValue(), key);
            OperationStatus status = database.get(null, key, data, LockMode.DEFAULT);
            if (!OperationStatus.SUCCESS.equals(status)) {
                String msg = String.format("node id %s not found", nodeID);
                throw new IllegalArgumentException(msg);

            }
            Coordinate c = CoordinateBinding.entryToCoord(data);
            coords[index] = c;
        }
        return coords;
    }
}
