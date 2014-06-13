/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.osm.cli.commands;

import java.io.File;
import java.util.List;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.geogit.api.GeoGIT;
import org.geogit.api.GlobalContextBuilder;
import org.geogit.api.RevFeature;
import org.geogit.api.RevFeatureType;
import org.geogit.api.TestPlatform;
import org.geogit.api.plumbing.RevObjectParse;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.plumbing.diff.DiffEntry.ChangeType;
import org.geogit.api.porcelain.DiffOp;
import org.geogit.cli.GeogitCLI;
import org.geogit.cli.test.functional.general.CLITestContextBuilder;
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 *
 */
public class OSMHistoryImportTest extends Assert {

    private GeogitCLI cli;

    private String fakeOsmApiUrl;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        ConsoleReader consoleReader = new ConsoleReader(System.in, System.out,
                new UnsupportedTerminal());
        cli = new GeogitCLI(consoleReader);
        fakeOsmApiUrl = getClass().getResource("../../internal/history/01_10").toExternalForm();

        File workingDirectory = tempFolder.getRoot();
        TestPlatform platform = new TestPlatform(workingDirectory);
        GlobalContextBuilder.builder = new CLITestContextBuilder(platform);
        cli.setPlatform(platform);
        cli.execute("init");
        assertTrue(new File(workingDirectory, ".geogit").exists());
    }

    @After
    public void tearDown() {
        if (cli != null) {
            cli.close();
        }
    }

    @Test
    public void test() throws Exception {
        cli.execute("config", "user.name", "Gabriel Roldan");
        cli.execute("config", "user.email", "groldan@opengeo.org");
        cli.execute("osm", "import-history", fakeOsmApiUrl, "--to", "9");

        GeoGIT geogit = cli.getGeogit();
        List<DiffEntry> changes = ImmutableList.copyOf(geogit.command(DiffOp.class)
                .setOldVersion("HEAD^").setNewVersion("HEAD").call());
        assertEquals(1, changes.size());
        DiffEntry entry = changes.get(0);
        assertEquals(ChangeType.MODIFIED, entry.changeType());
        assertEquals("node/20", entry.getOldObject().path());
        assertEquals("node/20", entry.getNewObject().path());

        Optional<RevFeature> oldRevFeature = geogit.command(RevObjectParse.class)
                .setObjectId(entry.getOldObject().objectId()).call(RevFeature.class);
        Optional<RevFeature> newRevFeature = geogit.command(RevObjectParse.class)
                .setObjectId(entry.getNewObject().objectId()).call(RevFeature.class);
        assertTrue(oldRevFeature.isPresent());
        assertTrue(newRevFeature.isPresent());

        Optional<RevFeatureType> type = geogit.command(RevObjectParse.class)
                .setObjectId(entry.getOldObject().getMetadataId()).call(RevFeatureType.class);
        assertTrue(type.isPresent());

        FeatureType featureType = type.get().type();

        CoordinateReferenceSystem expected = CRS.decode("EPSG:4326", true);
        CoordinateReferenceSystem actual = featureType.getCoordinateReferenceSystem();

        assertTrue(actual.toString(), CRS.equalsIgnoreMetadata(expected, actual));
    }

}
