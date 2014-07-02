/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.repository;

import java.util.concurrent.ExecutorService;

import org.geogit.api.Platform;

public class FileNodeIndexTest extends AbstractNodeIndexTest {

    @Override
    protected NodeIndex createIndex(Platform platform, ExecutorService executorService) {
        return new FileNodeIndex(platform, executorService);
    }

}
