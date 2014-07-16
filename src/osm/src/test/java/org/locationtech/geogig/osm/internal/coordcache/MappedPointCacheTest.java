/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.osm.internal.coordcache;

import org.locationtech.geogig.api.Platform;

public class MappedPointCacheTest extends PointCacheTest {

    @Override
    protected MappedPointCache createCache(Platform platform) {
        return new MappedPointCache(platform);
    }

}
