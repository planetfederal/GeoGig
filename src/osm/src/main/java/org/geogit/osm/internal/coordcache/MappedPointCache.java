/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.osm.internal.coordcache;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.geogit.api.Platform;
import org.geogit.api.plumbing.ResolveGeogitDir;
import org.geogit.osm.internal.OSMCoordinateSequence;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class MappedPointCache implements PointCache {

    private static final Random RANDOM = new Random();

    private File parentDir;

    private MappedIndex index;

    public MappedPointCache(Platform platform) {
        final Optional<File> geogitDir = new ResolveGeogitDir(platform).getFile();
        checkState(geogitDir.isPresent());
        this.parentDir = new File(new File(geogitDir.get(), "tmp"), "pointcache_"
                + Math.abs(RANDOM.nextInt()));
        checkState(parentDir.exists() || parentDir.mkdirs());
        this.parentDir.deleteOnExit();

        try {
            this.index = new MappedIndex(parentDir);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void put(Long nodeId, OSMCoordinateSequence coord) {
        Preconditions.checkNotNull(nodeId, "id is null");
        Preconditions.checkNotNull(coord, "coord is null");
        Preconditions.checkArgument(1 == coord.size(), "coord list size is not 1");

        index.putCoordinate(nodeId, coord.ordinates());
    }

    @Override
    public OSMCoordinateSequence get(List<Long> ids) {
        Preconditions.checkNotNull(ids, "ids is null");

        OSMCoordinateSequence sequence = index.build(ids);
        return sequence;
    }

    @Override
    public synchronized void dispose() {
        if (index == null) {
            return;
        }
        try {
            index.close();
        } finally {
            index = null;
        }
        parentDir.delete();
    }

}
