/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.cli.porcelain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.cli.GeogigCLI;
import org.locationtech.geogig.cli.InvalidParameterException;
import org.mockito.exceptions.base.MockitoException;

public class GeoJsonImportTest extends Assert {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private GeogigCLI cli;

    @Before
    public void setUp() throws Exception {
        ConsoleReader consoleReader = new ConsoleReader(System.in, System.out,
                new UnsupportedTerminal());
        cli = new GeogigCLI(consoleReader);

        setUpGeogit(cli);
    }

    @After
    public void tearDown() throws Exception {
        cli.close();
    }

    @Test
    public void testImport() throws Exception {
        GeoJsonImport importCommand = new GeoJsonImport();
        importCommand.geoJSONList = new ArrayList<String>();
        importCommand.geoJSONList.add(GeoJsonImport.class.getResource("sample.geojson").getFile());
        importCommand.run(cli);
    }

    @Test
    public void testImportFileNotExist() throws Exception {
        GeoJsonImport importCommand = new GeoJsonImport();
        importCommand.geoJSONList = new ArrayList<String>();
        importCommand.geoJSONList.add("file://nonexistent.geojson");
        importCommand.run(cli);
    }

    @Test
    public void testImportNullGeoJSONList() throws Exception {
        GeoJsonImport importCommand = new GeoJsonImport();
        exception.expect(InvalidParameterException.class);
        importCommand.run(cli);
    }

    @Test
    public void testImportEmptyGeoJSONList() throws Exception {
        GeoJsonImport importCommand = new GeoJsonImport();
        importCommand.geoJSONList = new ArrayList<String>();
        exception.expect(InvalidParameterException.class);
        importCommand.run(cli);
    }

    @Test
    public void testImportHelp() throws Exception {
        GeoJsonImport importCommand = new GeoJsonImport();
        importCommand.help = true;
        importCommand.run(cli);
    }

    @Test
    public void testImportGeomNameAndGeomNameAuto() throws Exception {
        GeoJsonImport importCommand = new GeoJsonImport();
        importCommand.geoJSONList = new ArrayList<String>();
        importCommand.geoJSONList.add(GeoJsonImport.class.getResource("sample.geojson").getFile());
        importCommand.geomName = "the_geom";
        importCommand.geomNameAuto = true;
        exception.expect(InvalidParameterException.class);
        importCommand.run(cli);
    }

    @Test
    public void testImportException() throws Exception {
        ConsoleReader consoleReader = new ConsoleReader(System.in, System.out,
                new UnsupportedTerminal());
        GeogigCLI mockCli = spy(new GeogigCLI(consoleReader));

        setUpGeogit(mockCli);

        when(mockCli.getConsole()).thenThrow(new MockitoException("Exception"));
        GeoJsonImport importCommand = new GeoJsonImport();
        importCommand.geoJSONList = new ArrayList<String>();
        importCommand.geoJSONList.add(ShpImport.class.getResource("sample.geojson").getFile());
        exception.expect(MockitoException.class);
        importCommand.run(mockCli);
    }

    private void setUpGeogit(GeogigCLI cli) throws Exception {
        final File userhome = tempFolder.newFolder("mockUserHomeDir");
        final File workingDir = tempFolder.newFolder("mockWorkingDir");
        tempFolder.newFolder("mockWorkingDir/.geogit");

        final Platform platform = mock(Platform.class);
        when(platform.pwd()).thenReturn(workingDir);
        when(platform.getUserHome()).thenReturn(userhome);

        cli.setPlatform(platform);
    }

}
