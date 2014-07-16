/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.locationtech.geogig.geotools.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.GeometryBuilder;
import org.junit.Test;
import org.locationtech.geogig.api.NodeRef;
import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.plumbing.LsTreeOp;
import org.locationtech.geogig.api.plumbing.LsTreeOp.Strategy;
import org.locationtech.geogig.api.porcelain.BranchCreateOp;
import org.locationtech.geogig.api.porcelain.CommitOp;
import org.locationtech.geogig.test.integration.RepositoryTestCase;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class GeoGigDataStoreTest extends RepositoryTestCase {

    private GeoGigDataStore dataStore;

    @Override
    protected void setUpInternal() throws Exception {
        dataStore = new GeoGigDataStore(geogig);
    }

    @Override
    protected void tearDownInternal() throws Exception {
        dataStore.dispose();
        dataStore = null;
    }

    @Test
    public void testDispose() {
        assertTrue(geogig.isOpen());
        dataStore.dispose();
        assertFalse(geogig.isOpen());
    }

    private List<String> getTypeNames(String head) {
        Iterator<NodeRef> typeTrees = geogig.command(LsTreeOp.class)
                .setStrategy(Strategy.TREES_ONLY).setReference(head).call();
        List<String> typeNames = Lists.newArrayList(Iterators.transform(typeTrees,
                new Function<NodeRef, String>() {

                    @Override
                    public String apply(NodeRef input) {
                        return input.name();
                    }
                }));
        return typeNames;
    }

    @Test
    public void testCreateSchema() throws IOException {
        final SimpleFeatureType featureType = super.linesType;
        dataStore.createSchema(featureType);

        List<String> typeNames;
        typeNames = getTypeNames(Ref.HEAD);
        assertEquals(1, typeNames.size());
        assertEquals(linesName, typeNames.get(0));

        dataStore.createSchema(super.pointsType);

        typeNames = getTypeNames(Ref.HEAD);
        assertEquals(2, typeNames.size());
        assertTrue(typeNames.contains(linesName));
        assertTrue(typeNames.contains(pointsName));

        try {
            dataStore.createSchema(super.pointsType);
            fail("Expected IOException on existing type");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("already exists"));
        }
    }

    @Test
    public void testCreateSchemaOnBranch() throws IOException {
        final String branchName = "testBranch";
        geogig.command(BranchCreateOp.class).setName(branchName).setOrphan(true).call();

        dataStore.setHead(branchName);
        final SimpleFeatureType featureType = super.linesType;
        dataStore.createSchema(featureType);

        List<String> typeNames;
        typeNames = getTypeNames(Ref.HEAD);
        assertTrue(typeNames.isEmpty());

        typeNames = getTypeNames(branchName);
        assertEquals(1, typeNames.size());
        assertEquals(linesName, typeNames.get(0));

        dataStore.createSchema(super.pointsType);

        typeNames = getTypeNames(Ref.HEAD);
        assertTrue(typeNames.isEmpty());

        typeNames = getTypeNames(branchName);
        assertEquals(2, typeNames.size());
        assertTrue(typeNames.contains(linesName));
        assertTrue(typeNames.contains(pointsName));
    }

    @Test
    public void testGetNames() throws Exception {

        assertEquals(0, dataStore.getNames().size());

        insertAndAdd(points1);
        assertEquals(0, dataStore.getNames().size());
        commit();

        assertEquals(1, dataStore.getNames().size());

        insertAndAdd(lines1);
        assertEquals(1, dataStore.getNames().size());
        commit();

        assertEquals(2, dataStore.getNames().size());

        List<Name> names = dataStore.getNames();
        // ContentDataStore doesn't support native namespaces
        // assertTrue(names.contains(RepositoryTestCase.linesTypeName));
        // assertTrue(names.contains(RepositoryTestCase.pointsTypeName));
        assertTrue(names.contains(new NameImpl(pointsName)));
        assertTrue(names.contains(new NameImpl(linesName)));
    }

    @Test
    public void testGetTypeNames() throws Exception {

        assertEquals(0, dataStore.getTypeNames().length);

        insertAndAdd(lines1);
        assertEquals(0, dataStore.getTypeNames().length);
        commit();

        assertEquals(1, dataStore.getTypeNames().length);

        insertAndAdd(points1);
        assertEquals(1, dataStore.getTypeNames().length);
        commit();

        assertEquals(2, dataStore.getTypeNames().length);

        List<String> simpleNames = Arrays.asList(dataStore.getTypeNames());

        assertTrue(simpleNames.contains(linesName));
        assertTrue(simpleNames.contains(pointsName));
    }

    @Test
    public void testGetSchemaName() throws Exception {
        try {
            dataStore.getSchema(RepositoryTestCase.linesTypeName);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("does not exist"));
        }

        insertAndAdd(lines1);
        try {
            dataStore.getSchema(RepositoryTestCase.linesTypeName);
            fail("Expected IOE as type hasn't been committed");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("does not exist"));
        }
        commit();
        SimpleFeatureType lines = dataStore.getSchema(RepositoryTestCase.linesTypeName);
        assertEquals(super.linesType, lines);

        try {
            dataStore.getSchema(RepositoryTestCase.pointsTypeName);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(true);
        }

        insertAndAdd(points1);
        commit();
        SimpleFeatureType points = dataStore.getSchema(RepositoryTestCase.pointsTypeName);
        assertEquals(super.pointsType, points);
    }

    private void commit() {
        geogig.command(CommitOp.class).call();
    }

    @Test
    public void testGetSchemaProvidedNamespace() throws Exception {
        String namespace = "http://www.geogig.org/test";
        dataStore.setNamespaceURI(namespace);
        insertAndAdd(lines1);
        commit();
        SimpleFeatureType lines = dataStore.getSchema(RepositoryTestCase.linesTypeName);
        Name expectedName = new NameImpl(namespace, linesName);
        assertEquals(expectedName, lines.getName());
        assertEquals(super.linesType.getAttributeDescriptors(), lines.getAttributeDescriptors());

        insertAndAdd(points1);
        commit();
        SimpleFeatureType points = dataStore.getSchema(RepositoryTestCase.pointsTypeName);
        assertEquals(new NameImpl(namespace, pointsName), points.getName());
        assertEquals(super.pointsType.getAttributeDescriptors(), points.getAttributeDescriptors());
    }

    @Test
    public void testGetSchemaString() throws Exception {
        try {
            dataStore.getSchema(RepositoryTestCase.linesName);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(true);
        }

        insertAndAdd(lines1);
        commit();
        SimpleFeatureType lines = dataStore.getSchema(RepositoryTestCase.linesName);
        assertEquals(super.linesType.getAttributeDescriptors(), lines.getAttributeDescriptors());

        try {
            dataStore.getSchema(RepositoryTestCase.pointsName);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(true);
        }

        insertAndAdd(points1);
        commit();
        SimpleFeatureType points = dataStore.getSchema(RepositoryTestCase.pointsName);
        assertEquals(super.pointsType, points);
    }

    @Test
    public void testGetFeatureSourceName() throws Exception {
        try {
            dataStore.getFeatureSource(RepositoryTestCase.linesTypeName);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(true);
        }

        SimpleFeatureSource source;

        insertAndAdd(lines1);
        try {
            dataStore.getFeatureSource(RepositoryTestCase.linesTypeName);
            fail("Expected IOE as feature typ is not committed yet");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("does not exist"));
        }
        commit();
        source = dataStore.getFeatureSource(RepositoryTestCase.linesTypeName);
        assertTrue(source instanceof GeogigFeatureStore);

        try {
            dataStore.getFeatureSource(RepositoryTestCase.pointsTypeName);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(true);
        }

        insertAndAdd(points1);
        commit();
        source = dataStore.getFeatureSource(RepositoryTestCase.pointsTypeName);
        assertTrue(source instanceof GeogigFeatureStore);
    }

    @Test
    public void testGetFeatureSourceString() throws Exception {
        try {
            dataStore.getFeatureSource(RepositoryTestCase.linesName);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(true);
        }

        SimpleFeatureSource source;

        insertAndAdd(lines1);
        commit();
        source = dataStore.getFeatureSource(RepositoryTestCase.linesName);
        assertTrue(source instanceof GeogigFeatureStore);

        try {
            dataStore.getFeatureSource(RepositoryTestCase.pointsName);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(true);
        }

        insertAndAdd(points1);
        commit();
        source = dataStore.getFeatureSource(RepositoryTestCase.pointsName);
        assertTrue(source instanceof GeogigFeatureStore);
    }

    @Test
    public void testFeatureWriterAppend() throws Exception {
        dataStore.createSchema(linesType);

        Transaction tx = new DefaultTransaction();
        FeatureWriter<SimpleFeatureType, SimpleFeature> fw = dataStore.getFeatureWriterAppend(
                linesTypeName.getLocalPart(), tx);

        LineString line = new GeometryBuilder().lineString(0, 0, 1, 1);
        SimpleFeature f = (SimpleFeature) fw.next();
        f.setAttribute("sp", "foo");
        f.setAttribute("ip", 10);
        f.setAttribute("pp", line);

        fw.write();
        fw.close();

        tx.commit();

        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                .getFeatureSource(linesTypeName);
        assertEquals(1, source.getCount(null));

        FeatureReader<SimpleFeatureType, SimpleFeature> r = dataStore.getFeatureReader(new Query(
                linesTypeName.getLocalPart()), Transaction.AUTO_COMMIT);
        assertTrue(r.hasNext());

        f = r.next();
        assertEquals("foo", f.getAttribute("sp"));
        assertEquals(10, f.getAttribute("ip"));
        assertTrue(line.equals((Geometry) f.getAttribute("pp")));
    }
}
