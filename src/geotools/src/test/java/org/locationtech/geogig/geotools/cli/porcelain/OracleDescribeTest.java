/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.cli.porcelain;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

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
import org.locationtech.geogig.cli.CommandFailedException;
import org.locationtech.geogig.cli.GeogitCLI;
import org.mockito.exceptions.base.MockitoException;

public class OracleDescribeTest extends Assert {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private GeogitCLI cli;

    @Before
    public void setUp() throws Exception {
        ConsoleReader consoleReader = new ConsoleReader(System.in, System.out,
                new UnsupportedTerminal());
        cli = new GeogitCLI(consoleReader);

        setUpGeogit(cli);
    }

    @After
    public void tearDown() throws Exception {
        cli.close();
    }

    @Test
    public void testDescribe() throws Exception {
        OracleDescribe describeCommand = new OracleDescribe();
        describeCommand.table = "table1";
        describeCommand.dataStoreFactory = TestHelper.createTestFactory();
        describeCommand.run(cli);
    }

    @Test
    public void testInvalidDatabaseParams() throws Exception {
        OracleDescribe describeCommand = new OracleDescribe();
        describeCommand.commonArgs.host = "nonexistent";
        describeCommand.table = "table1";
        exception.expect(CommandFailedException.class);
        describeCommand.run(cli);
    }

    @Test
    public void testDescribeNonexistentTable() throws Exception {
        OracleDescribe describeCommand = new OracleDescribe();
        describeCommand.table = "nonexistent";
        describeCommand.dataStoreFactory = TestHelper.createTestFactory();
        exception.expect(CommandFailedException.class);
        describeCommand.run(cli);
    }

    @Test
    public void testNoTable() throws Exception {
        OracleDescribe describeCommand = new OracleDescribe();
        describeCommand.table = "";
        describeCommand.dataStoreFactory = TestHelper.createTestFactory();
        exception.expect(CommandFailedException.class);
        describeCommand.run(cli);
    }

    @Test
    public void testNullDataStore() throws Exception {
        OracleDescribe describeCommand = new OracleDescribe();
        describeCommand.table = "table1";
        describeCommand.dataStoreFactory = TestHelper.createNullTestFactory();
        exception.expect(CommandFailedException.class);
        describeCommand.run(cli);
    }

    @Test
    public void testDescribeException() throws Exception {
        ConsoleReader consoleReader = new ConsoleReader(System.in, System.out,
                new UnsupportedTerminal());
        GeogitCLI mockCli = spy(new GeogitCLI(consoleReader));

        setUpGeogit(mockCli);

        when(mockCli.getConsole()).thenThrow(new MockitoException("Exception"));
        OracleDescribe describeCommand = new OracleDescribe();
        describeCommand.table = "table1";
        describeCommand.dataStoreFactory = TestHelper.createTestFactory();
        exception.expect(MockitoException.class);
        describeCommand.run(mockCli);
    }

    @Test
    public void testFlushException() throws Exception {
        ConsoleReader consoleReader = spy(new ConsoleReader(System.in, System.out,
                new UnsupportedTerminal()));
        GeogitCLI testCli = new GeogitCLI(consoleReader);

        setUpGeogit(testCli);

        doThrow(new IOException("Exception")).when(consoleReader).flush();

        OracleDescribe describeCommand = new OracleDescribe();
        describeCommand.table = "table1";
        describeCommand.dataStoreFactory = TestHelper.createTestFactory();
        exception.expect(Exception.class);
        describeCommand.run(testCli);
    }

    private void setUpGeogit(GeogitCLI cli) throws Exception {
        final File userhome = tempFolder.newFolder("mockUserHomeDir");
        final File workingDir = tempFolder.newFolder("mockWorkingDir");
        tempFolder.newFolder("mockWorkingDir/.geogit");

        final Platform platform = mock(Platform.class);
        when(platform.pwd()).thenReturn(workingDir);
        when(platform.getUserHome()).thenReturn(userhome);

        cli.setPlatform(platform);
    }
}
