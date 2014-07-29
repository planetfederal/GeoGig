/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.cli;

import org.locationtech.geogig.cli.CLICommandExtension;
import org.locationtech.geogig.geotools.cli.porcelain.PGDescribe;
import org.locationtech.geogig.geotools.cli.porcelain.PGExport;
import org.locationtech.geogig.geotools.cli.porcelain.PGImport;
import org.locationtech.geogig.geotools.cli.porcelain.PGList;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

/**
 * {@link CLICommandExtension} that provides a {@link JCommander} for PostGIS specific commands.
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogig pg <command> <args>...}
 * </ul>
 * 
 * @see PGImport
 * @see PGList
 * @see PGDescribe
 * @see PGExport
 */
@Parameters(commandNames = "pg", commandDescription = "GeoGig/PostGIS integration utilities")
public class PGCommandProxy implements CLICommandExtension {

    /**
     * @return the JCommander parser for this extension
     * @see JCommander
     */
    @Override
    public JCommander getCommandParser() {
        JCommander commander = new JCommander();
        commander.setProgramName("geogig pg");
        commander.addCommand("import", new PGImport());
        commander.addCommand("list", new PGList());
        commander.addCommand("describe", new PGDescribe());
        commander.addCommand("export", new PGExport());

        return commander;
    }
}
