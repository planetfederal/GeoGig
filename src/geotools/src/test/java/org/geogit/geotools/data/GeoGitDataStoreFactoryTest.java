/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.geotools.data;

import static org.geogit.geotools.data.GeoGitDataStoreFactory.CREATE;
import static org.geogit.geotools.data.GeoGitDataStoreFactory.REPOSITORY;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.geogit.test.integration.RepositoryTestCase;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

public class GeoGitDataStoreFactoryTest extends RepositoryTestCase {

    private GeoGitDataStoreFactory factory;

    private File repoDirectory;

    @Override
    protected void setUpInternal() throws Exception {
        factory = new GeoGitDataStoreFactory();
        repoDirectory = geogit.getPlatform().pwd();
    }

    @Test
    public void testFactorySpi() {
        Iterator<GeoGitDataStoreFactory> filtered = Iterators.filter(
                DataStoreFinder.getAvailableDataStores(), GeoGitDataStoreFactory.class);
        assertTrue(filtered.hasNext());
        assertTrue(filtered.next() instanceof GeoGitDataStoreFactory);
    }

    @Test
    public void testDataStoreFinder() throws Exception {
        Map<String, ? extends Serializable> params;
        DataStore dataStore;

        params = ImmutableMap.of();
        dataStore = DataStoreFinder.getDataStore(params);
        assertNull(dataStore);

        params = ImmutableMap.of(REPOSITORY.key, repoDirectory);
        dataStore = DataStoreFinder.getDataStore(params);
        assertNotNull(dataStore);
        assertTrue(dataStore instanceof GeoGitDataStore);
    }

    @Test
    public void testCanProcess() {
        final File workingDir = repositoryTempFolder.getRoot();

        Map<String, Serializable> params = ImmutableMap.of();
        assertFalse(factory.canProcess(params));
        params = ImmutableMap.of(REPOSITORY.key,
                (Serializable) (workingDir.getName() + "/shouldntExist"));
        assertFalse(factory.canProcess(params));

        params = ImmutableMap.of(REPOSITORY.key, (Serializable) workingDir.getAbsolutePath());
        assertTrue(factory.canProcess(params));

        params = ImmutableMap.of(REPOSITORY.key, (Serializable) repoDirectory.getPath());
        assertTrue(factory.canProcess(params));

        params = ImmutableMap.of(REPOSITORY.key, (Serializable) repoDirectory.getAbsolutePath());
        assertTrue(factory.canProcess(params));

        params = ImmutableMap.of(REPOSITORY.key, (Serializable) repoDirectory);
        assertTrue(factory.canProcess(params));
    }

    @Test
    public void testCreateDataStoreNonExistentDirectory() {
        Map<String, Serializable> params;

        File root = repositoryTempFolder.getRoot();
        params = ImmutableMap
                .of(REPOSITORY.key, (Serializable) (root.getName() + "/shouldntExist"));
        try {
            factory.createDataStore(params);
            fail("Expectd IOE on non existing directory");
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCreateDataStoreNotARepositoryDir() {
        Map<String, Serializable> params;

        File f = repositoryTempFolder.newFolder("someDir");
        params = ImmutableMap.of(REPOSITORY.key, (Serializable) f);
        try {
            factory.createDataStore(params);
            fail("Expectd IOE on non existing repository");
        } catch (IOException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("not a geogit repository"));
        }
    }

    @Test
    public void testCreateDataStore() throws IOException {
        Map<String, Serializable> params;

        params = ImmutableMap.of(REPOSITORY.key, (Serializable) repoDirectory.getAbsolutePath());

        GeoGitDataStore store = factory.createDataStore(params);
        assertNotNull(store);

    }

    @Test
    public void testCreateNewDataStore() throws IOException {
        Map<String, Serializable> params;

        String newRepoDir = repositoryTempFolder.newFolder("datastore").getAbsolutePath();

        params = ImmutableMap.of(REPOSITORY.key, (Serializable) newRepoDir);

        GeoGitDataStore store = factory.createNewDataStore(params);
        assertNotNull(store);

    }

    @Test
    public void testCreateOption() throws Exception {
        String newRepoDir = repositoryTempFolder.newFolder("datastore").getAbsolutePath();

        Map<String, Serializable> params = ImmutableMap.of(REPOSITORY.key,
                (Serializable) newRepoDir, CREATE.key, true);

        assertTrue(factory.canProcess(params));
        GeoGitDataStore store = factory.createDataStore(params);
        assertNotNull(store);
    }

    @Test
    public void testCreateOptionDirectoryExists() throws Exception {
        File newRepoDir = repositoryTempFolder.newFolder("datastore");

        Map<String, Serializable> params = ImmutableMap.of(REPOSITORY.key,
                (Serializable) newRepoDir, CREATE.key, true);
        assertTrue(factory.canProcess(params));
        GeoGitDataStore store = factory.createDataStore(params);
        assertNotNull(store);
    }
}
