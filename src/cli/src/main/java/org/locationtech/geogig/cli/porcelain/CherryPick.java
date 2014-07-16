/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.cli.porcelain;

import java.util.List;

import org.locationtech.geogig.api.GeoGIT;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.plumbing.RevParse;
import org.locationtech.geogig.api.porcelain.CherryPickOp;
import org.locationtech.geogig.cli.AbstractCommand;
import org.locationtech.geogig.cli.CLICommand;
import org.locationtech.geogig.cli.GeogitCLI;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;

/**
 * Given an existing commit, apply the change it introduces, recording a new commit . This requires
 * your working tree to be clean (no modifications from the HEAD commit).
 * <p>
 * CLI proxy for {@link CherryPickOp}
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogit cherry-pick <commitish>}
 * </ul>
 * 
 * @see CherryPickOp
 */
@Parameters(commandNames = "cherry-pick", commandDescription = "Apply the changes introduced by existing commits")
public class CherryPick extends AbstractCommand implements CLICommand {

    @Parameter(description = "<commitish>...")
    private List<String> commits = Lists.newArrayList();

    @Override
    public void runInternal(GeogitCLI cli) {
        final GeoGIT geogit = cli.getGeogit();
        checkParameter(commits.size() > 0, "No commits specified.");
        checkParameter(commits.size() < 2, "Too many commits specified.");

        CherryPickOp cherryPick = geogit.command(CherryPickOp.class);

        Optional<ObjectId> commitId;
        commitId = geogit.command(RevParse.class).setRefSpec(commits.get(0)).call();
        checkParameter(commitId.isPresent(), "Commit not found '%s'", commits.get(0));
        cherryPick.setCommit(Suppliers.ofInstance(commitId.get()));

        cherryPick.call();

    }
}
