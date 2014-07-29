/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.memory;

import org.geogit.api.Platform;
import org.geogit.storage.GraphDatabaseTest;

public class HeapGraphDatabaseTest extends GraphDatabaseTest {

    @Override
    protected HeapGraphDatabase createDatabase(Platform platform) {
        return new HeapGraphDatabase(platform);
    }

}
