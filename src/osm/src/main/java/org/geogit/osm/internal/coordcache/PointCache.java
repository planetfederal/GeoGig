/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.osm.internal.coordcache;

import java.util.List;

import org.geogit.osm.internal.OSMCoordinateSequence;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;

/**
 * A Temporary cache of coordinates for the osm import operation to save node coordinates by node
 * ids, abd query coordinate sequences when building way's {@link LineString}s by the list of node
 * ids in each way.
 * 
 */
public interface PointCache {

    /**
     * Saves a coordinate for a Node in the cache
     */
    public abstract void put(Long nodeId, OSMCoordinateSequence coord);

    /**
     * Gets a new coordinate sequence out of the coordinates assiciated with each node id in the
     * argument list.
     * 
     * @param ids the list of node ids from a OSM way primitive
     * @return the coordinate sequence built up from the coordinates associated with each node id in
     *         the argument list
     */
    public CoordinateSequence get(List<Long> ids);

    /**
     * Closes and releases any resource used by the cache. This method is idempotent.
     */
    public void dispose();
}