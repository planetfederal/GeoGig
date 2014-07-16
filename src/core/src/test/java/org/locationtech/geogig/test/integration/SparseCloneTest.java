/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.data.DataUtilities;
import org.geotools.feature.NameImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.RevCommit;
import org.locationtech.geogig.api.RevObject;
import org.locationtech.geogig.api.plumbing.RevObjectParse;
import org.locationtech.geogig.api.plumbing.UpdateRef;
import org.locationtech.geogig.api.porcelain.BranchCreateOp;
import org.locationtech.geogig.api.porcelain.CheckoutOp;
import org.locationtech.geogig.api.porcelain.CloneOp;
import org.locationtech.geogig.api.porcelain.CommitOp;
import org.locationtech.geogig.api.porcelain.ConfigOp;
import org.locationtech.geogig.api.porcelain.ConfigOp.ConfigAction;
import org.locationtech.geogig.api.porcelain.ConfigOp.ConfigScope;
import org.locationtech.geogig.api.porcelain.LogOp;
import org.locationtech.geogig.api.porcelain.MergeOp;
import org.locationtech.geogig.api.porcelain.MergeOp.MergeReport;
import org.locationtech.geogig.api.porcelain.PullOp;
import org.locationtech.geogig.api.porcelain.PushOp;
import org.locationtech.geogig.remote.AbstractMappedRemoteRepo;
import org.locationtech.geogig.remote.LocalMappedRemoteRepo;
import org.locationtech.geogig.remote.RemoteRepositoryTestCase;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;

public class SparseCloneTest extends RemoteRepositoryTestCase {

    protected static final String idR1 = "Roads.1";

    protected static final String idR2 = "Roads.2";

    protected static final String idR3 = "Roads.3";

    protected static final String idC1 = "Cities.1";

    protected static final String idC2 = "Cities.2";

    protected static final String idC3 = "Cities.3";

    protected static final String citiesNs = "http://geogit.cities";

    protected static final String citiesName = "Cities";

    protected static final String citiesTypeSpec = "name:String,population:Integer,pp:Point:srid=4326";

    protected static final Name citiesTypeName = new NameImpl("http://geogit.cities", citiesName);

    protected SimpleFeatureType citiesType;

    protected Feature city1;

    protected Feature city1_modified;

    protected Feature city2;

    protected Feature city3;

    protected static final String roadsNs = "http://geogit.roads";

    protected static final String roadsName = "Roads";

    protected static final String roadsTypeSpec = "name:String,length:Integer,pp:LineString:srid=4326";

    protected static final Name roadsTypeName = new NameImpl("http://geogit.roads", roadsName);

    protected SimpleFeatureType roadsType;

    protected Feature road1;

    protected Feature road2;

    protected Feature road3;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        citiesType = DataUtilities.createType(citiesNs, citiesName, citiesTypeSpec);

        city1 = feature(citiesType, idC1, "San Francisco", new Integer(200000),
                "POINT(10.5559899 -71.6524294)");
        city1_modified = feature(citiesType, idC1, "San Francisco", new Integer(200000),
                "POINT(37.76169 -122.44791)");
        city2 = feature(citiesType, idC2, "San Diego", new Integer(350000),
                "POINT(32.7443 -117.2157)");
        city3 = feature(citiesType, idC3, "Los Angeles", new Integer(1000000),
                "POINT(34.0455 -118.2380)");

        roadsType = DataUtilities.createType(roadsNs, roadsName, roadsTypeSpec);

        road1 = feature(roadsType, idR1, "Main Street", new Integer(236),
                "LINESTRING (37.76169 -122.44791, 34.0455 -118.2380)");
        road2 = feature(roadsType, idR2, "Long Road", new Integer(2845),
                "LINESTRING (37.76169 -122.44791, 32.7443 -117.2157)");
        road3 = feature(roadsType, idR3, "San Rafael Way", new Integer(528),
                "LINESTRING (34.0455 -118.2380, 37.76169 -122.44791)");
    }

    private void createFilterFile(Map<String, String> filters) {
        String filterFile = "";
        for (Entry<String, String> entry : filters.entrySet()) {
            String featurePath = entry.getKey();
            String filter = entry.getValue();
            filterFile += "[" + featurePath + "]\n";
            filterFile += "type = CQL\n";
            filterFile += "filter = " + filter + "\n";
        }
        try {
            String path = this.localGeogit.geogit.getPlatform().pwd().getAbsolutePath()
                    + "/.geogit/";
            PrintWriter out = new PrintWriter(path + "filter.ini");
            out.println(filterFile);
            out.close();
            localGeogit.geogit.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET)
                    .setName("sparse.filter").setValue("filter.ini").setScope(ConfigScope.LOCAL)
                    .call();

            LocalMappedRemoteRepo remoteRepo = spy(new LocalMappedRemoteRepo(
                    remoteGeogit.getInjector(), remoteGeogit.envHome.getCanonicalFile(),
                    localGeogit.repo));

            doNothing().when(remoteRepo).close();

            remoteRepo.setGeoGit(remoteGeogit.geogit);
            this.remoteRepo = remoteRepo;
        } catch (Exception e) {
        }
    }

    @Test
    public void testSparseClone() throws Exception {

        Map<String, String> filter = new HashMap<String, String>();
        filter.put("default", "BBOX(pp,30, -125, 40, -110,'EPSG:4326')");
        filter.put("Cities", "BBOX(pp,33, -125, 40, -110,'EPSG:4326')");
        createFilterFile(filter);
        // Commit several features to the remote
        List<Feature> features = Arrays.asList(city1, city2, city3, road1, road2, road3);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(remoteGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = remoteGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = remoteGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        // Make sure the remote has all of the commits
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expected, logged);

        // Make sure the local repository has no commits prior to clone
        logs = localGeogit.geogit.command(LogOp.class).call();
        assertNotNull(logs);
        assertFalse(logs.hasNext());

        // clone from the remote
        CloneOp clone = clone();
        clone.setDepth(0);
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).setBranch("master").call();

        // The features that match the filter are "Cities.3", "Roads.1", "Roads.2", and "Roads.3",
        // the "Cities.1" commit should be present since it added the "Cities" tree, but "Cities.1"
        // should not be present in the tree.

        // Make sure the local repository got the correct commits
        logs = localGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(5, logged.size());
        assertEquals("Roads.3", logged.get(0).getMessage());
        assertFalse(expected.get(0).getId().equals(logged.get(0).getId()));
        assertEquals("Roads.2", logged.get(1).getMessage());
        assertFalse(expected.get(1).getId().equals(logged.get(1).getId()));
        assertEquals("Roads.1", logged.get(2).getMessage());
        assertFalse(expected.get(2).getId().equals(logged.get(2).getId()));
        assertEquals("Cities.3", logged.get(3).getMessage());
        assertFalse(expected.get(3).getId().equals(logged.get(3).getId()));
        assertEquals("Cities.1", logged.get(4).getMessage());
        assertFalse(expected.get(5).getId().equals(logged.get(4).getId()));

        assertExists(localGeogit, oids.get(city3), oids.get(road1), oids.get(road2),
                oids.get(road3));
        assertNotExists(localGeogit, oids.get(city1), oids.get(city2));
    }

    @Test
    public void testSparseCloneAllMatch() throws Exception {

        Map<String, String> filter = new HashMap<String, String>();
        filter.put("default", "BBOX(pp,0, -125, 40, -70,'EPSG:4326')");
        createFilterFile(filter);
        // Commit several features to the remote
        List<Feature> features = Arrays.asList(city1, city2, city3, road1, road2, road3);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(remoteGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = remoteGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = remoteGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        // Make sure the remote has all of the commits
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expected, logged);

        // Make sure the local repository has no commits prior to clone
        logs = localGeogit.geogit.command(LogOp.class).call();
        assertNotNull(logs);
        assertFalse(logs.hasNext());

        // clone from the remote
        CloneOp clone = clone();
        clone.setDepth(0);
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).setBranch("master").call();

        // Because all features match the filter, the history should be identical

        // Make sure the local repository got the correct commits
        logs = localGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expected, logged);

        assertExists(localGeogit, oids.get(city1), oids.get(city2), oids.get(city3),
                oids.get(road1), oids.get(road2), oids.get(road3));
    }

    @Test
    public void testSparseCloneOnlyFirstMatch() throws Exception {

        Map<String, String> filter = new HashMap<String, String>();
        filter.put("default", "BBOX(pp,9, -80, 15, -70,'EPSG:4326')");
        createFilterFile(filter);
        // Commit several features to the remote
        List<Feature> features = Arrays.asList(city1, city2, city3, road1, road2, road3);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(remoteGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = remoteGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = remoteGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        // Make sure the remote has all of the commits
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expected, logged);

        // Make sure the local repository has no commits prior to clone
        logs = localGeogit.geogit.command(LogOp.class).call();
        assertNotNull(logs);
        assertFalse(logs.hasNext());

        // clone from the remote
        CloneOp clone = clone();
        clone.setDepth(0);
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).setBranch("master").call();

        // Because only the first feature matches (Cities.1), the first commit should be the same,
        // there will also be the commit that adds the "Roads" tree but no features, and finally an
        // "Empty Placeholder Commit".

        // Make sure the local repository got the correct commits
        logs = localGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(3, logged.size());
        assertEquals(AbstractMappedRemoteRepo.PLACEHOLDER_COMMIT_MESSAGE, logged.get(0)
                .getMessage());
        assertFalse(expected.get(0).getId().equals(logged.get(0).getId()));
        assertEquals("Roads.1", logged.get(1).getMessage());
        assertFalse(expected.get(2).getId().equals(logged.get(1).getId()));
        assertEquals("Cities.1", logged.get(2).getMessage());
        assertTrue(expected.get(5).getId().equals(logged.get(2).getId()));

        assertExists(localGeogit, oids.get(city1));
        assertNotExists(localGeogit, oids.get(city2), oids.get(city3), oids.get(road1),
                oids.get(road2), oids.get(road3));
    }

    @Test
    public void testFeatureMovingOutOfAOI() throws Exception {

        Map<String, String> filter = new HashMap<String, String>();
        filter.put("default", "BBOX(pp,9, -80, 15, -70,'EPSG:4326')");
        createFilterFile(filter);
        // Commit several features to the remote
        List<Feature> features = Arrays.asList(city1, city1_modified);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(remoteGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = remoteGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = remoteGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        // Make sure the remote has all of the commits
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expected, logged);

        // Make sure the local repository has no commits prior to clone
        logs = localGeogit.geogit.command(LogOp.class).call();
        assertNotNull(logs);
        assertFalse(logs.hasNext());

        // clone from the remote
        CloneOp clone = clone();
        clone.setDepth(0);
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).setBranch("master").call();

        // Because Cities.1 is first in our filter, then is modified to be outside the filter, it
        // should continue to be tracked. Therefore our histories should match.

        // Make sure the local repository got the correct commits
        logs = localGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expected, logged);

        assertExists(localGeogit, oids.get(city1), oids.get(city1_modified));
    }

    @Test
    public void testFeatureMovingIntoAOI() throws Exception {

        Map<String, String> filter = new HashMap<String, String>();
        filter.put("Cities", "BBOX(pp,30, -125, 40, -110,'EPSG:4326')");
        createFilterFile(filter);
        // Commit several features to the remote
        List<Feature> features = Arrays.asList(city2, city1, city3, city1_modified);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(remoteGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = remoteGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = remoteGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        // Make sure the remote has all of the commits
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expected, logged);

        // Make sure the local repository has no commits prior to clone
        logs = localGeogit.geogit.command(LogOp.class).call();
        assertNotNull(logs);
        assertFalse(logs.hasNext());

        // clone from the remote
        CloneOp clone = clone();
        clone.setDepth(0);
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).setBranch("master").call();

        // Cities.1 initially lies outside the filter, so the commit that adds it will not be part
        // of the sparse clone. Later the feature is moved into the AOI so it will be added at that
        // time.

        // Make sure the local repository got the correct commits
        logs = localGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(3, logged.size());
        assertEquals("Cities.1", logged.get(0).getMessage());
        assertFalse(expected.get(0).getId().equals(logged.get(0).getId()));
        assertEquals("Cities.3", logged.get(1).getMessage());
        assertFalse(expected.get(1).getId().equals(logged.get(1).getId()));
        assertEquals("Cities.2", logged.get(2).getMessage());
        assertTrue(expected.get(3).getId().equals(logged.get(2).getId()));

        assertExists(localGeogit, oids.get(city2), oids.get(city3), oids.get(city1_modified));
        assertNotExists(localGeogit, oids.get(city1));
    }

    @Test
    public void testPullCommitThatPassesFilter() throws Exception {
        setupSparseClone();
        // Add a commit that passes our filter to the remote.
        ObjectId oId = insertAndAdd(remoteGeogit.geogit, city1_modified);
        final RevCommit commit = remoteGeogit.geogit.command(CommitOp.class)
                .setMessage(city1_modified.getIdentifier().toString()).call();
        Optional<RevObject> childObject = remoteGeogit.geogit.command(RevObjectParse.class)
                .setObjectId(oId).call();
        assertTrue(childObject.isPresent());
        assertEquals(commit,
                remoteGeogit.geogit.getRepository().objectDatabase().getCommit(commit.getId()));
        System.err.println(commit.getId());
        PullOp pull = pull();
        pull.call();

        Iterator<RevCommit> logs = localGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals("Cities.1", logged.get(0).getMessage());
        assertFalse(commit.getId().equals(logged.get(0).getId()));

        assertExists(localGeogit, oId);
    }

    @Test
    public void testPullCommitThatDoesNotPassFilter() throws Exception {
        setupSparseClone();
        // Add a commit that passes our filter to the remote.
        ObjectId oId = insertAndAdd(remoteGeogit.geogit, city1);
        final RevCommit commit = remoteGeogit.geogit.command(CommitOp.class)
                .setMessage(city1.getIdentifier().toString()).call();
        Optional<RevObject> childObject = remoteGeogit.geogit.command(RevObjectParse.class)
                .setObjectId(oId).call();
        assertTrue(childObject.isPresent());

        PullOp pull = pull();
        pull.call();

        Iterator<RevCommit> logs = localGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(AbstractMappedRemoteRepo.PLACEHOLDER_COMMIT_MESSAGE, logged.get(0)
                .getMessage());
        assertFalse(commit.getId().equals(logged.get(0).getId()));

        assertNotExists(localGeogit, oId);
    }

    @Test
    public void testPushCommitsFromSparseClone() throws Exception {
        setupSparseClone();
        // Add some commits to the local (sparse) repository
        List<Feature> features = Arrays.asList(city1, city1_modified, road3);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(localGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = localGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = localGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        PushOp push = push();
        push.setAll(true).call();

        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals("Roads.3", logged.get(0).getMessage());
        assertFalse(expected.get(0).getId().equals(logged.get(0).getId()));
        assertEquals("Cities.1", logged.get(1).getMessage());
        assertFalse(expected.get(1).getId().equals(logged.get(1).getId()));
        assertEquals("Cities.1", logged.get(2).getMessage());
        assertFalse(expected.get(2).getId().equals(logged.get(2).getId()));

        assertExists(remoteGeogit, oids.get(city1), oids.get(city1_modified), oids.get(road3));
    }

    @Test
    public void testPushSparseMerge() throws Exception {
        setupSparseClone();
        // create a branch off an early commit
        Iterator<RevCommit> logs = localGeogit.geogit.command(LogOp.class).call();
        RevCommit initialCommit = logs.next();
        ObjectId masterCommit = initialCommit.getId();
        while (logs.hasNext()) {
            initialCommit = logs.next();
        }
        localGeogit.geogit.command(BranchCreateOp.class).setName("Branch1").setAutoCheckout(true)
                .setSource(initialCommit.getId().toString()).call();

        // Add some commits to the local (sparse) repository
        List<Feature> features = Arrays.asList(city1, city1_modified, road3);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(localGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = localGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = localGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        // Merge master into Branch1
        MergeOp merge = localGeogit.geogit.command(MergeOp.class);
        MergeReport report = merge.addCommit(Suppliers.ofInstance(masterCommit))
                .setMessage("Merge").call();

        // Update master to the new merge commit
        localGeogit.geogit.command(UpdateRef.class).setName("refs/heads/master")
                .setNewValue(report.getMergeCommit().getId()).call();

        // Checkout master
        localGeogit.geogit.command(CheckoutOp.class).setSource("master").call();

        PushOp push = push();
        push.addRefSpec("refs/heads/master").call();

        logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals("Merge", logged.get(0).getMessage());
        assertFalse(report.getMergeCommit().getId().equals(logged.get(0).getId()));

        // Although we merged "Roads.2" commit into the "Roads.3" commit, making the "Roads.2"
        // commit the second parent, they should have been swapped when pushing to the full
        // repository to prevent any sparse data from being lost.
        ObjectId parent1Id = logged.get(0).getParentIds().get(0);
        ObjectId parent2Id = logged.get(0).getParentIds().get(1);

        RevCommit parent1 = remoteGeogit.geogit.getRepository().getCommit(parent1Id);
        assertNotNull(parent1);
        assertEquals("Roads.2", parent1.getMessage());
        RevCommit parent2 = remoteGeogit.geogit.getRepository().getCommit(parent2Id);
        assertNotNull(parent2);
        assertEquals("Roads.3", parent2.getMessage());

        // Verify they weren't swapped in the original
        parent1Id = report.getMergeCommit().getParentIds().get(0);
        parent2Id = report.getMergeCommit().getParentIds().get(1);

        parent1 = localGeogit.geogit.getRepository().getCommit(parent1Id);
        assertNotNull(parent1);
        assertEquals("Roads.3", parent1.getMessage());
        parent2 = localGeogit.geogit.getRepository().getCommit(parent2Id);
        assertNotNull(parent2);
        assertEquals("Roads.2", parent2.getMessage());

        assertExists(remoteGeogit, oids.get(city1), oids.get(city1_modified), oids.get(road3));
    }

    @Test
    public void testPushSparseMergeScenario2() throws Exception {
        setupSparseClone();
        // create a branch off an early commit
        Iterator<RevCommit> logs = localGeogit.geogit.command(LogOp.class).call();
        RevCommit initialCommit = logs.next();
        while (logs.hasNext()) {
            initialCommit = logs.next();
        }
        localGeogit.geogit.command(BranchCreateOp.class).setName("Branch1").setAutoCheckout(true)
                .setSource(initialCommit.getId().toString()).call();

        // Add some commits to the local (sparse) repository
        List<Feature> features = Arrays.asList(city1, city1_modified, road3);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(localGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = localGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = localGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        // Checkout master
        localGeogit.geogit.command(CheckoutOp.class).setSource("master").call();

        // Merge Branch1 into master
        MergeOp merge = localGeogit.geogit.command(MergeOp.class);
        MergeReport report = merge.addCommit(Suppliers.ofInstance(expected.get(0).getId()))
                .setMessage("Merge").call();

        PushOp push = push();
        push.addRefSpec("refs/heads/master").call();

        logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals("Merge", logged.get(0).getMessage());
        assertFalse(report.getMergeCommit().getId().equals(logged.get(0).getId()));

        // Because we merged Branch1 into the "sparse" master, we don't need to swap the parents, so
        // the history should look the same.
        ObjectId parent1Id = logged.get(0).getParentIds().get(0);
        ObjectId parent2Id = logged.get(0).getParentIds().get(1);

        RevCommit parent1 = remoteGeogit.geogit.getRepository().getCommit(parent1Id);
        assertNotNull(parent1);
        assertEquals("Roads.2", parent1.getMessage());
        RevCommit parent2 = remoteGeogit.geogit.getRepository().getCommit(parent2Id);
        assertNotNull(parent2);
        assertEquals("Roads.3", parent2.getMessage());

        // Verify they weren't swapped in the original
        parent1Id = report.getMergeCommit().getParentIds().get(0);
        parent2Id = report.getMergeCommit().getParentIds().get(1);

        parent1 = localGeogit.geogit.getRepository().getCommit(parent1Id);
        assertNotNull(parent1);
        assertEquals("Roads.2", parent1.getMessage());
        parent2 = localGeogit.geogit.getRepository().getCommit(parent2Id);
        assertNotNull(parent2);
        assertEquals("Roads.3", parent2.getMessage());

        assertExists(remoteGeogit, oids.get(city1), oids.get(city1_modified), oids.get(road3));
    }

    @Test
    public void testSparseCloneWithNoBranchSpecified() throws Exception {
        Map<String, String> filter = new HashMap<String, String>();
        filter.put("default", "BBOX(pp,9, -80, 15, -70,'EPSG:4326')");
        createFilterFile(filter);

        CloneOp clone = clone();
        exception.expect(IllegalArgumentException.class);
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).call();
    }

    @Test
    public void testSparseShallowClone() throws Exception {
        Map<String, String> filter = new HashMap<String, String>();
        filter.put("default", "BBOX(pp,9, -80, 15, -70,'EPSG:4326')");
        createFilterFile(filter);

        CloneOp clone = clone();
        clone.setDepth(3).setBranch("master");
        exception.expect(IllegalStateException.class);
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).call();
    }

    private void setupSparseClone() throws Exception {

        Map<String, String> filter = new HashMap<String, String>();
        filter.put("default", "BBOX(pp,30, -125, 40, -110,'EPSG:4326')");
        filter.put("Cities", "BBOX(pp,33, -125, 40, -110,'EPSG:4326')");
        createFilterFile(filter);
        // Commit several features to the remote
        List<Feature> features = Arrays.asList(city3, road1, city2, road2);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();
        Map<Feature, ObjectId> oids = new HashMap<Feature, ObjectId>();

        for (Feature f : features) {
            ObjectId oId = insertAndAdd(remoteGeogit.geogit, f);
            oids.put(f, oId);
            final RevCommit commit = remoteGeogit.geogit.command(CommitOp.class)
                    .setMessage(f.getIdentifier().toString()).call();
            expected.addFirst(commit);
            Optional<RevObject> childObject = remoteGeogit.geogit.command(RevObjectParse.class)
                    .setObjectId(oId).call();
            assertTrue(childObject.isPresent());
        }

        // Make sure the remote has all of the commits
        Iterator<RevCommit> logs = remoteGeogit.geogit.command(LogOp.class).call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals(expected, logged);

        // Make sure the local repository has no commits prior to clone
        logs = localGeogit.geogit.command(LogOp.class).call();
        assertNotNull(logs);
        assertFalse(logs.hasNext());

        // clone from the remote
        CloneOp clone = clone();
        clone.setDepth(0);
        clone.setRepositoryURL(remoteGeogit.envHome.getCanonicalPath()).setBranch("master").call();

        logs = localGeogit.geogit.command(LogOp.class).call();
        logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }

        assertEquals("Roads.2", logged.get(0).getMessage());
        assertFalse(expected.get(0).getId().equals(logged.get(0).getId()));
        assertEquals(expected.get(2).getId(), logged.get(1).getId());
        assertEquals(expected.get(3).getId(), logged.get(2).getId());

    }

    private void assertExists(GeogitContainer geogit, ObjectId... features) {
        for (ObjectId object : features) {
            assertTrue(geogit.geogit.getRepository().blobExists(object));
        }
    }

    private void assertNotExists(GeogitContainer geogit, ObjectId... features) {
        for (ObjectId object : features) {
            assertFalse(geogit.geogit.getRepository().blobExists(object));
        }
    }

}
