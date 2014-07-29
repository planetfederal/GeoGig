/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.geotools.plumbing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.locationtech.geogig.geotools.cli.porcelain.TestHelper;

import com.google.common.base.Optional;

public class ListOpTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullDataStore() throws Exception {
        ListOp list = new ListOp();
        exception.expect(GeoToolsOpException.class);
        list.call();
    }

    @Test
    public void testEmptyDataStore() throws Exception {
        ListOp list = new ListOp();
        list.setDataStore(TestHelper.createEmptyTestFactory().createDataStore(null));
        Optional<List<String>> features = list.call();
        assertFalse(features.isPresent());
    }

    @Test
    public void testTypeNameException() throws Exception {
        ListOp list = new ListOp();
        list.setDataStore(TestHelper.createFactoryWithGetNamesException().createDataStore(null));
        exception.expect(GeoToolsOpException.class);
        list.call();
    }

    @Test
    public void testList() throws Exception {
        ListOp list = new ListOp();
        list.setDataStore(TestHelper.createTestFactory().createDataStore(null));
        Optional<List<String>> features = list.call();
        assertTrue(features.isPresent());

        assertTrue(features.get().contains("table1"));
        assertTrue(features.get().contains("table2"));
    }
}
