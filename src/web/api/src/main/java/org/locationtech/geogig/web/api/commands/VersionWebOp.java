/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.web.api.commands;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.porcelain.VersionInfo;
import org.locationtech.geogig.api.porcelain.VersionOp;
import org.locationtech.geogig.web.api.AbstractWebAPICommand;
import org.locationtech.geogig.web.api.CommandContext;
import org.locationtech.geogig.web.api.CommandResponse;
import org.locationtech.geogig.web.api.ResponseWriter;

/**
 * Interface for the Version operation in the GeoGit.
 * 
 * Web interface for {@link VersionOp}, {@link VersionInfo}
 */

public class VersionWebOp extends AbstractWebAPICommand {

    /**
     * Runs the command and builds the appropriate response.
     * 
     * @param context - the context to use for this command
     */
    @Override
    public void run(CommandContext context) {
        final Context geogit = this.getCommandLocator(context);

        final VersionInfo info = geogit.command(VersionOp.class).call();

        context.setResponseContent(new CommandResponse() {
            @Override
            public void write(ResponseWriter out) throws Exception {
                out.start();
                out.writeElement("ProjectVersion", info.getProjectVersion());
                out.writeElement("BuildTime", info.getBuildTime());
                out.writeElement("BuildUserName", info.getBuildUserName());
                out.writeElement("BuildUserEmail", info.getBuildUserEmail());
                out.writeElement("GitBranch", info.getBranch());
                out.writeElement("GitCommitID", info.getCommitId());
                out.writeElement("GitCommitTime", info.getCommitTime());
                out.writeElement("GitCommitAuthorName", info.getCommitUserName());
                out.writeElement("GitCommitAuthorEmail", info.getCommitUserEmail());
                out.writeElement("GitCommitMessage", info.getCommitMessageFull());
                out.finish();
            }
        });
    }

}
