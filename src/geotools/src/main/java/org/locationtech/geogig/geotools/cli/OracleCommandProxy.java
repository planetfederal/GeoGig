/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.cli;

import org.locationtech.geogig.cli.CLICommandExtension;
import org.locationtech.geogig.geotools.cli.porcelain.OracleDescribe;
import org.locationtech.geogig.geotools.cli.porcelain.OracleExport;
import org.locationtech.geogig.geotools.cli.porcelain.OracleImport;
import org.locationtech.geogig.geotools.cli.porcelain.OracleList;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

/**
 * {@link CLICommandExtension} that provides a {@link JCommander} for Oracle specific commands.
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogit oracle <command> <args>...}
 * </ul>
 * 
 * @see OracleImport
 * @see OracleList
 * @see OracleDescribe
 * @see OracleExport
 */

@Parameters(commandNames = "oracle", commandDescription = "GeoGit/Oracle integration utilities")
public class OracleCommandProxy implements CLICommandExtension {

    /**
     * @return the JCommander parser for this extension
     * @see JCommander
     */
    @Override
    public JCommander getCommandParser() {
        JCommander commander = new JCommander();
        commander.setProgramName("geogit oracle");
        commander.addCommand("import", new OracleImport());
        commander.addCommand("list", new OracleList());
        commander.addCommand("describe", new OracleDescribe());
        commander.addCommand("export", new OracleExport());

        return commander;
    }
}
