/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.geotools.cli;

import org.locationtech.geogig.cli.CLICommandExtension;
import org.locationtech.geogig.geotools.cli.porcelain.GeoJsonExport;
import org.locationtech.geogig.geotools.cli.porcelain.GeoJsonImport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

/**
 * {@link CLICommandExtension} that provides a {@link JCommander} for GeoJSON specific commands.
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogit geojson <command> <args>...}
 * </ul>
 * 
 * @see GeoJsonImport
 */

@Parameters(commandNames = "geojson", commandDescription = "GeoGit/GeoJSON integration utilities")
public class GeoJsonCommandProxy implements CLICommandExtension {

    /**
     * @return the JCommander parser for this extension
     * @see JCommander
     */
    @Override
    public JCommander getCommandParser() {
        JCommander commander = new JCommander();
        commander.setProgramName("geogit geojson");
        commander.addCommand("import", new GeoJsonImport());
        commander.addCommand("export", new GeoJsonExport());
        return commander;
    }

}
