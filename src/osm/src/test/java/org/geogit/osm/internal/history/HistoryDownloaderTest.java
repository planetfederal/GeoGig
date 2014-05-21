/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.osm.internal.history;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 */
public class HistoryDownloaderTest extends Assert {

    private HistoryDownloader localResourcesDownloader;

    private ExecutorService executor;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File downloadFolder;

    @Before
    public void setUp() throws Exception {
        String osmAPIUrl = getClass().getResource("01_10").toExternalForm();
        long initialChangeset = 1;
        long finalChangeset = 10;

        downloadFolder = tempFolder.newFolder("downloads");

        executor = Executors.newFixedThreadPool(6);
        localResourcesDownloader = new HistoryDownloader(osmAPIUrl, downloadFolder,
                initialChangeset, finalChangeset, executor, false);
    }

    @Test
    public void testFetchChangesets() throws Exception {
        List<Changeset> changesets = Lists.newArrayList();
        Optional<Changeset> next;
        while ((next = localResourcesDownloader.fetchNextChangeset()).isPresent()) {
            Changeset changeset = next.get();
            changesets.add(changeset);
        }

        assertEquals(10, changesets.size());
    }

    @Test
    public void testFetchChangesetContents() throws Exception {
        Iterator<Change> changes;
        List<Change> list;

        Iterator<Changeset> changesetsIterator = new AbstractIterator<Changeset>() {
            @Override
            protected Changeset computeNext() {
                Optional<Changeset> next = localResourcesDownloader.fetchNextChangeset();
                if (next.isPresent()) {
                    return next.get();
                }
                return super.endOfData();
            }
        };

        ArrayList<Changeset> changesets = Lists.newArrayList(changesetsIterator);
        assertEquals(10, changesets.size());

        changes = changesets.get(0).getChanges().get().get();
        list = Lists.newArrayList(changes);
        assertEquals(3, list.size());// see 01_10/1/download.xml
        assertTrue(list.get(0).getNode().isPresent());
        assertTrue(list.get(1).getNode().isPresent());
        assertTrue(list.get(2).getWay().isPresent());

        // 01_10/10/download.xml is empty
        changes = changesets.get(9).getChanges().get().get();
        assertFalse(changes.hasNext());

        // 01_10/5/download.xml
        changes = changesets.get(4).getChanges().get().get();
        list = Lists.newArrayList(changes);
        assertEquals(4, list.size());// see 01_10/1/download.xml
        assertTrue(list.get(0).getNode().isPresent());
        assertTrue(list.get(1).getNode().isPresent());
        assertTrue(list.get(2).getNode().isPresent());
        assertTrue(list.get(3).getWay().isPresent());
    }

    @Ignore
    @Test
    public void testFetchFailingChangesetsOnline() throws Exception {
        // String osmAPIUrl = "http://api06.dev.openstreetmap.org/api/0.6/";
        String osmAPIUrl = "http://api.openstreetmap.org/api/0.6/";
        long initialChangeset = 749;// this one gives a 500 internal server error
        long finalChangeset = 750;

        HistoryDownloader onlineDownloader = new HistoryDownloader(osmAPIUrl, downloadFolder,
                initialChangeset, finalChangeset, executor, false);

        Optional<Changeset> next = onlineDownloader.fetchNextChangeset();
        assertTrue(next.isPresent());
        Changeset changeset = next.get();
        assertEquals(749, changeset.getId());

        Iterator<Change> changes;
        changes = changeset.getChanges().get().get();
        assertNotNull(changes);
        assertFalse(changes.hasNext());

        next = onlineDownloader.fetchNextChangeset();
        assertTrue(next.isPresent());
        changeset = next.get();
        assertEquals(750, changeset.getId());

        changes = changeset.getChanges().get().get();
        assertNotNull(changes);
        assertTrue(changes.hasNext());
    }

    @Ignore
    @Test
    public void testFetchChangesetsOnline() throws Exception {
        String osmAPIUrl = "http://api06.dev.openstreetmap.org/api/0.6/";
        // String osmAPIUrl = "http://api.openstreetmap.org/api/0.6/";
        long initialChangeset = 1;
        long finalChangeset = 30;

        HistoryDownloader onlineDownloader = new HistoryDownloader(osmAPIUrl, downloadFolder,
                initialChangeset, finalChangeset, executor, false);

        Optional<Changeset> next;
        List<Changeset> changesets = Lists.newArrayList();
        Map<Long, List<Change>> changes = Maps.newTreeMap();

        while ((next = onlineDownloader.fetchNextChangeset()).isPresent()) {
            Changeset changeset = next.get();
            changesets.add(changeset);
            Iterator<Change> iterator = changeset.getChanges().get().get();
            changes.put(Long.valueOf(changeset.getId()), Lists.newArrayList(iterator));
        }

        assertEquals(30, changesets.size());
    }
}
