/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration.sqlite;

import static org.locationtech.geogig.test.integration.sqlite.XerialTests.injector;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.TestPlatform;


public class XerialFindCommonAncestorTest extends
        org.locationtech.geogig.test.integration.FindCommonAncestorTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Override
    protected Context createInjector() {
        return injector(new TestPlatform(temp.getRoot()));
    }
}
