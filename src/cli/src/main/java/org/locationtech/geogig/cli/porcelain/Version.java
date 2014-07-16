/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.cli.porcelain;

import java.io.IOException;

import javax.annotation.Nullable;

import jline.console.ConsoleReader;

import org.locationtech.geogig.api.GeoGIG;
import org.locationtech.geogig.api.porcelain.VersionInfo;
import org.locationtech.geogig.api.porcelain.VersionOp;
import org.locationtech.geogig.cli.CLICommand;
import org.locationtech.geogig.cli.GeogigCLI;
import org.locationtech.geogig.cli.annotation.ReadOnly;
import org.locationtech.geogig.cli.annotation.RequiresRepository;

import com.beust.jcommander.Parameters;
import com.google.common.base.Throwables;

/**
 * This command displays the GeoGit version information.
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogit [--]version}
 * </ul>
 */
@ReadOnly
@RequiresRepository(false)
@Parameters(commandNames = { "--version", "version" }, commandDescription = "Display GeoGit version information")
public class Version implements CLICommand {

    private ConsoleReader console;

    private static int PROPERTY_NAME_WIDTH = 24;

    /**
     * Executes the version command.
     * 
     * @param cli
     * @see org.locationtech.geogig.cli.CLICommand#run(org.locationtech.geogig.cli.GeogigCLI)
     */
    public void run(GeogigCLI cli) {
        GeoGIG geogit = cli.getGeogit();
        if (geogit == null) {
            geogit = new GeoGIG();
        }
        this.console = cli.getConsole();
        VersionInfo info = geogit.command(VersionOp.class).call();

        try {
            printVersionProperty("Project Version", info.getProjectVersion());
            printVersionProperty("Build Time", info.getBuildTime());
            printVersionProperty("Build User Name", info.getBuildUserName());
            printVersionProperty("Build User Email", info.getBuildUserEmail());
            printVersionProperty("Git Branch", info.getBranch());
            printVersionProperty("Git Commit ID", info.getCommitId());
            printVersionProperty("Git Commit Time", info.getCommitTime());
            printVersionProperty("Git Commit Author Name", info.getCommitUserName());
            printVersionProperty("Git Commit Author Email", info.getCommitUserEmail());
            printVersionProperty("Git Commit Message", info.getCommitMessageFull());
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    private void printVersionProperty(String propertyName, @Nullable String propertyValue)
            throws IOException {
        console.print(String.format("%1$" + PROPERTY_NAME_WIDTH + "s : ", propertyName));
        console.print((propertyValue != null ? propertyValue : "Unspecified") + "\n");
    }
}
