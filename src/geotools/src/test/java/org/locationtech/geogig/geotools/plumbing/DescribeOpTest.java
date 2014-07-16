/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.geotools.plumbing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.locationtech.geogig.geotools.cli.porcelain.TestHelper;

import com.google.common.base.Optional;

public class DescribeOpTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullDataStore() throws Exception {
        DescribeOp describe = new DescribeOp();
        describe.setTable("table1");
        exception.expect(GeoToolsOpException.class);
        describe.call();
    }

    @Test
    public void testNullTable() throws Exception {
        DescribeOp describe = new DescribeOp();
        describe.setDataStore(TestHelper.createEmptyTestFactory().createDataStore(null));
        exception.expect(GeoToolsOpException.class);
        describe.call();
    }

    @Test
    public void testEmptyTable() throws Exception {
        DescribeOp describe = new DescribeOp();
        describe.setTable("");
        describe.setDataStore(TestHelper.createEmptyTestFactory().createDataStore(null));
        exception.expect(GeoToolsOpException.class);
        describe.call();
    }

    @Test
    public void testEmptyDataStore() throws Exception {
        DescribeOp describe = new DescribeOp();
        describe.setDataStore(TestHelper.createEmptyTestFactory().createDataStore(null));
        describe.setTable("table1");
        Optional<Map<String, String>> features = describe.call();
        assertFalse(features.isPresent());
    }

    @Test
    public void testTypeNameException() throws Exception {
        DescribeOp describe = new DescribeOp();
        describe.setDataStore(TestHelper.createFactoryWithGetNamesException().createDataStore(null));
        describe.setTable("table1");
        exception.expect(GeoToolsOpException.class);
        describe.call();
    }

    @Test
    public void testGetFeatureSourceException() throws Exception {
        DescribeOp describe = new DescribeOp();
        describe.setDataStore(TestHelper.createFactoryWithGetFeatureSourceException()
                .createDataStore(null));
        describe.setTable("table1");
        exception.expect(GeoToolsOpException.class);
        describe.call();
    }

    @Test
    public void testDescribe() throws Exception {
        DescribeOp describe = new DescribeOp();
        describe.setDataStore(TestHelper.createTestFactory().createDataStore(null));
        describe.setTable("table1");
        Optional<Map<String, String>> properties = describe.call();
        assertTrue(properties.isPresent());

        assertEquals("Point", properties.get().get("geom"));
        assertEquals("String", properties.get().get("label"));
    }
}
