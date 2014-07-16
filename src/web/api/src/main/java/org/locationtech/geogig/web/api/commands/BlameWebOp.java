/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.web.api.commands;

import javax.xml.stream.XMLStreamException;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.plumbing.RevParse;
import org.locationtech.geogig.api.porcelain.BlameException;
import org.locationtech.geogig.api.porcelain.BlameOp;
import org.locationtech.geogig.api.porcelain.BlameReport;
import org.locationtech.geogig.web.api.AbstractWebAPICommand;
import org.locationtech.geogig.web.api.CommandContext;
import org.locationtech.geogig.web.api.CommandResponse;
import org.locationtech.geogig.web.api.CommandSpecException;
import org.locationtech.geogig.web.api.ResponseWriter;

import com.google.common.base.Optional;

/**
 * Interface for the Blame operation in the GeoGit.
 * 
 * Web interface for {@link BlameOp}, {@link BlameReport}
 */

public class BlameWebOp extends AbstractWebAPICommand {

    private String path;

    private String branchOrCommit;

    /**
     * Mutator for the branchOrCommit variable
     * 
     * @param branchOrCommit - the branch or commit to blame from
     */
    public void setCommit(String branchOrCommit) {
        this.branchOrCommit = branchOrCommit;
    }

    /**
     * Mutator for the path variable
     * 
     * @param path - the path of the feature
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Runs the command and builds the appropriate response.
     * 
     * @param context - the context to use for this command
     */
    @Override
    public void run(CommandContext context) {
        final Context geogit = this.getCommandLocator(context);

        Optional<ObjectId> commit = Optional.absent();
        if (branchOrCommit != null) {
            commit = geogit.command(RevParse.class).setRefSpec(branchOrCommit).call();
            if (!commit.isPresent()) {
                throw new CommandSpecException("Could not resolve branch or commit");
            }
        }

        try {
            final BlameReport report = geogit.command(BlameOp.class).setPath(path)
                    .setCommit(commit.orNull()).call();

            context.setResponseContent(new CommandResponse() {
                @Override
                public void write(ResponseWriter out) throws Exception {
                    out.start();
                    try {
                        out.writeBlameReport(report);
                    } catch (XMLStreamException e) {
                        throw new CommandSpecException("Error writing stream.");
                    }
                    out.finish();
                }
            });
        } catch (BlameException e) {
            switch (e.statusCode) {
            case PATH_NOT_FEATURE:
                throw new CommandSpecException("The supplied path does not resolve to a feature");
            case FEATURE_NOT_FOUND:
                throw new CommandSpecException("The supplied path does not exist");
            }
        }
    }

}
