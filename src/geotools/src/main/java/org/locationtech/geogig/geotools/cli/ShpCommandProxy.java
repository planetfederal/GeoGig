/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.cli;

import org.locationtech.geogig.cli.CLICommandExtension;
import org.locationtech.geogig.geotools.cli.porcelain.ShpExport;
import org.locationtech.geogig.geotools.cli.porcelain.ShpExportDiff;
import org.locationtech.geogig.geotools.cli.porcelain.ShpImport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

/**
 * {@link CLICommandExtension} that provides a {@link JCommander} for shapefile specific commands.
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogig shp <command> <args>...}
 * </ul>
 * 
 * @see ShpImport
 */
@Parameters(commandNames = "shp", commandDescription = "GeoGig/Shapefile integration utilities")
public class ShpCommandProxy implements CLICommandExtension {

    /**
     * @return the JCommander parser for this extension
     * @see JCommander
     */
    @Override
    public JCommander getCommandParser() {
        JCommander commander = new JCommander();
        commander.setProgramName("geogig shp");
        commander.addCommand("import", new ShpImport());
        commander.addCommand("export", new ShpExport());
        commander.addCommand("export-diff", new ShpExportDiff());
        return commander;
    }
}
