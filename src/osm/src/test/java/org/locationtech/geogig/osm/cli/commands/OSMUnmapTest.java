/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.osm.cli.commands;

import java.io.File;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.locationtech.geogig.api.GeoGIG;
import org.locationtech.geogig.api.GlobalContextBuilder;
import org.locationtech.geogig.api.RevFeature;
import org.locationtech.geogig.api.RevTree;
import org.locationtech.geogig.api.TestPlatform;
import org.locationtech.geogig.api.plumbing.RevObjectParse;
import org.locationtech.geogig.cli.GeogigCLI;
import org.locationtech.geogig.cli.test.functional.general.CLITestContextBuilder;
import org.locationtech.geogig.osm.internal.OSMImportOp;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class OSMUnmapTest extends Assert {

    private GeogigCLI cli;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        ConsoleReader consoleReader = new ConsoleReader(System.in, System.out,
                new UnsupportedTerminal());
        cli = new GeogigCLI(consoleReader);
        File workingDirectory = tempFolder.getRoot();
        TestPlatform platform = new TestPlatform(workingDirectory);
        GlobalContextBuilder.builder = new CLITestContextBuilder(platform);
        cli.setPlatform(platform);
        cli.execute("init");
        cli.execute("config", "user.name", "Gabriel Roldan");
        cli.execute("config", "user.email", "groldan@opengeo.org");
        assertTrue(new File(workingDirectory, ".geogit").exists());
        // import with mapping
        String filename = OSMImportOp.class.getResource("nodes.xml").getFile();
        File file = new File(filename);
        String mappingFilename = OSMMap.class.getResource("nodes_mapping_with_aliases.json")
                .getFile();
        File mappingFile = new File(mappingFilename);
        cli.execute("osm", "import", file.getAbsolutePath(), "--mapping",
                mappingFile.getAbsolutePath());
        GeoGIG geogit = cli.newGeoGIT();
        Optional<RevFeature> revFeature = geogit.command(RevObjectParse.class)
                .setRefSpec("WORK_HEAD:busstops/507464799").call(RevFeature.class);
        assertTrue(revFeature.isPresent());
        geogit.getRepository().workingTree().delete("node");
        Optional<RevTree> tree = geogit.command(RevObjectParse.class).setRefSpec("WORK_HEAD:node")
                .call(RevTree.class);
        assertFalse(tree.isPresent());
        geogit.close();
    }

    @Test
    public void testUnMapping() throws Exception {
        cli.execute("osm", "unmap", "busstops");
        GeoGIG geogit = cli.newGeoGIT();
        Optional<RevTree> tree = geogit.command(RevObjectParse.class).setRefSpec("HEAD:node")
                .call(RevTree.class);
        assertTrue(tree.isPresent());
        assertTrue(tree.get().size() > 0);
        Optional<RevFeature> unmapped = geogit.command(RevObjectParse.class)
                .setRefSpec("HEAD:node/507464799").call(RevFeature.class);
        assertTrue(unmapped.isPresent());
        ImmutableList<Optional<Object>> values = unmapped.get().getValues();
        assertEquals("POINT (7.1959361 50.739397)", values.get(6).get().toString());
        assertEquals(
                "VRS:gemeinde:BONN|VRS:ortsteil:Hoholz|VRS:ref:68566|bus:yes|highway:bus_stop|name:Gielgen|public_transport:platform",
                values.get(3).get().toString());
        geogit.close();
    }

}
