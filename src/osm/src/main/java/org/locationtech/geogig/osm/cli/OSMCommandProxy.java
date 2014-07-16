/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.osm.cli;

import org.locationtech.geogig.cli.CLICommandExtension;
import org.locationtech.geogig.osm.cli.commands.CreateOSMChangeset;
import org.locationtech.geogig.osm.cli.commands.OSMApplyDiff;
import org.locationtech.geogig.osm.cli.commands.OSMDownload;
import org.locationtech.geogig.osm.cli.commands.OSMExport;
import org.locationtech.geogig.osm.cli.commands.OSMExportPG;
import org.locationtech.geogig.osm.cli.commands.OSMExportSL;
import org.locationtech.geogig.osm.cli.commands.OSMExportShp;
import org.locationtech.geogig.osm.cli.commands.OSMHistoryImport;
import org.locationtech.geogig.osm.cli.commands.OSMImport;
import org.locationtech.geogig.osm.cli.commands.OSMMap;
import org.locationtech.geogig.osm.cli.commands.OSMUnmap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

/**
 * {@link CLICommandExtension} that provides a {@link JCommander} for osm specific commands.
 * 
 * @see OSMHistoryImport
 */
@Parameters(commandNames = "osm", commandDescription = "GeoGig/OpenStreetMap integration utilities")
public class OSMCommandProxy implements CLICommandExtension {

    @Override
    public JCommander getCommandParser() {
        JCommander commander = new JCommander();
        commander.setProgramName("geogig osm");
        commander.addCommand("import-history", new OSMHistoryImport());
        commander.addCommand("import", new OSMImport());
        commander.addCommand("export", new OSMExport());
        commander.addCommand("download", new OSMDownload());
        commander.addCommand("create-changeset", new CreateOSMChangeset());
        commander.addCommand("map", new OSMMap());
        commander.addCommand("unmap", new OSMUnmap());
        commander.addCommand("export-shp", new OSMExportShp());
        commander.addCommand("export-pg", new OSMExportPG());
        commander.addCommand("export-sl", new OSMExportSL());
        commander.addCommand("apply-diff", new OSMApplyDiff());
        return commander;
    }
}
