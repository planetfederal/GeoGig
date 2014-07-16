/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.cli.plumbing;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import jline.console.ConsoleReader;

import org.locationtech.geogig.api.GeoGIT;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.plumbing.ResolveGeogitDir;
import org.locationtech.geogig.cli.AbstractCommand;
import org.locationtech.geogig.cli.GeogitCLI;
import org.locationtech.geogig.cli.annotation.ReadOnly;
import org.locationtech.geogig.repository.Hints;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Determines if the current directory is inside a geogit repository.
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogit rev-parse --resolve-geogit-dir}: check if the current directory is inside a
 * geogit repository and print out the repository location
 * <li> {@code geogit rev-parse --is-inside-work-tree}: check if the current directory is inside a
 * geogit repository and print out the repository location
 * </ul>
 */
@ReadOnly
@Parameters(commandNames = "rev-parse", commandDescription = "Resolve parameters according to the arguments")
public class RevParse extends AbstractCommand {

    @Parameter(names = "--resolve-geogit-dir", description = "Check if the current directory is inside a geogit repository and print out the repository location")
    private boolean resolve_geogit_dir;

    @Parameter(names = "--is-inside-work-tree", description = "Check if the current directory is inside a geogit repository and print out the repository location")
    private boolean is_inside_work_tree;

    @Parameter(description = "[refSpec]... where refSpec is of the form [<object id>|<ref name>][^<parent index>]+[~<ancestor index>]+")
    private List<String> refSpecs = Lists.newArrayList();

    /**
     * Executes the rev-parse command using the provided options.
     */
    @Override
    protected void runInternal(GeogitCLI cli) throws IOException {
        GeoGIT geogit = cli.getGeogit();

        if (!refSpecs.isEmpty()) {
            checkParameter(!(resolve_geogit_dir || is_inside_work_tree),
                    "if refSpec is given, --resolve-geogit-dir or --is-inside-work-tree shall not be specified");
            ConsoleReader console = cli.getConsole();
            for (String refSpec : this.refSpecs) {
                Optional<ObjectId> resolved = geogit
                        .command(org.locationtech.geogig.api.plumbing.RevParse.class)
                        .setRefSpec(refSpec).call();
                checkParameter(resolved.isPresent(), "fatal: ambiguous argument '%s': "
                        + "unknown revision or path not in the working tree.", refSpec);
                console.println(resolved.get().toString());
            }
            console.flush();
            return;
        }

        boolean closeIt = false;
        if (null == geogit) {
            geogit = cli.newGeoGIT(Hints.readOnly());
            closeIt = true;
        }
        try {
            if (resolve_geogit_dir) {
                resolveGeogitDir(cli.getConsole(), geogit);
            } else if (is_inside_work_tree) {
                isInsideWorkTree(cli.getConsole(), geogit);
            }
        } finally {
            if (closeIt) {
                geogit.close();
            }
        }
    }

    private void isInsideWorkTree(ConsoleReader console, GeoGIT geogit) throws IOException {
        Optional<URL> repoUrl = geogit.command(ResolveGeogitDir.class).call();

        File pwd = geogit.getPlatform().pwd();

        if (repoUrl.isPresent()) {
            boolean insideWorkTree = !pwd.getAbsolutePath().contains(".geogit");
            console.println(String.valueOf(insideWorkTree));
        } else {
            console.println("Error: not a geogit repository (or any parent) '"
                    + pwd.getAbsolutePath() + "'");
        }
    }

    private void resolveGeogitDir(ConsoleReader console, GeoGIT geogit) throws IOException {

        URL repoUrl = geogit.command(ResolveGeogitDir.class).call().orNull();
        if (null == repoUrl) {
            File currDir = geogit.getPlatform().pwd();
            console.println("Error: not a geogit dir '"
                    + currDir.getCanonicalFile().getAbsolutePath() + "'");
        } else if ("file".equals(repoUrl.getProtocol())) {
            try {
                console.println(new File(repoUrl.toURI()).getCanonicalFile().getAbsolutePath());
            } catch (URISyntaxException e) {
                Throwables.propagate(e);
            }
        } else {
            console.println(repoUrl.toExternalForm());
        }
    }

}
