/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.memory;

import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.storage.GraphDatabaseTest;

public class HeapGraphDatabaseTest extends GraphDatabaseTest {

    @Override
    protected HeapGraphDatabase createDatabase(Platform platform) {
        return new HeapGraphDatabase(platform);
    }

}
