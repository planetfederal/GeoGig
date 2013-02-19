/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the LGPL 2.1 license, available at the root
 * application directory.
 */

package org.geogit.cli.porcelain;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Map.Entry;

import jline.console.ConsoleReader;

import org.geogit.api.porcelain.FetchResult.ChangedRef;
import org.geogit.api.porcelain.FetchResult.ChangedRef.ChangeTypes;
import org.geogit.api.porcelain.PullOp;
import org.geogit.api.porcelain.PullResult;
import org.geogit.cli.AbstractCommand;
import org.geogit.cli.CLICommand;
import org.geogit.cli.GeogitCLI;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Incorporates changes from a remote repository into the current branch.
 * <p>
 * More precisely, {@code geogit pull} runs {@code geogit fetch} with the given parameters and calls
 * {@code geogit merge} to merge the retrieved branch heads into the current branch. With
 * {@code --rebase}, it runs {@code geogit rebase} instead of {@code geogit merge}.
 * <p>
 * CLI proxy for {@link PullOp}
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogit pull [options] [<repository> [<refspec>...]]}
 * </ul>
 * 
 * @see PullOp
 */
@Parameters(commandNames = "pull", commandDescription = "Fetch from and merge with another repository or a local branch")
public class Pull extends AbstractCommand implements CLICommand {

    @Parameter(names = "--all", description = "Fetch all remotes.")
    private boolean all = false;

    @Parameter(names = "--rebase", description = "Rebase the current branch on top of the upstream branch after fetching.")
    private boolean rebase = false;

    @Parameter(description = "[<repository> [<refspec>...]]")
    private List<String> args;

    /**
     * Executes the pull command using the provided options.
     * 
     * @param cli
     * @see org.geogit.cli.AbstractCommand#runInternal(org.geogit.cli.GeogitCLI)
     */
    @Override
    public void runInternal(GeogitCLI cli) throws Exception {
        checkState(cli.getGeogit() != null, "Not a geogit repository: " + cli.getPlatform().pwd());

        PullOp pull = cli.getGeogit().command(PullOp.class);
        pull.setProgressListener(cli.getProgressListener());
        pull.setAll(all).setRebase(rebase);

        if (args != null) {
            if (args.size() > 0) {
                pull.setRemote(args.get(0));
            }
            for (int i = 1; i < args.size(); i++) {
                pull.addRefSpec(args.get(i));
            }
        }

        PullResult result = pull.call();

        ConsoleReader console = cli.getConsole();

        for (Entry<String, List<ChangedRef>> entry : result.getFetchResult().getChangedRefs()
                .entrySet()) {
            console.println("From " + entry.getKey());

            for (ChangedRef ref : entry.getValue()) {
                String line;
                if (ref.getType() == ChangeTypes.CHANGED_REF) {
                    line = "   " + ref.getOldRef().getObjectId().toString().substring(0, 7) + ".."
                            + ref.getNewRef().toString().substring(0, 7) + "     "
                            + ref.getNewRef().localName() + " -> " + ref.getNewRef().getName();
                } else if (ref.getType() == ChangeTypes.ADDED_REF) {
                    line = " * [new branch]     " + ref.getNewRef().localName() + " -> "
                            + ref.getNewRef().getName();
                } else {
                    line = " x [deleted]        (none) -> " + ref.getOldRef().getName();
                }
                console.println(line);
            }
        }

        console.println("From " + result.getRemoteName());
        if (result.getOldRef() == null) {
            console.println(" * [new branch]     " + result.getNewRef().localName() + " -> "
                    + result.getNewRef().getName());
        } else if (result.getOldRef().equals(result.getNewRef())) {
            console.println("Already up to date.");
        } else {
            String line = "   " + result.getOldRef().getObjectId().toString().substring(0, 7)
                    + ".." + result.getNewRef().toString().substring(0, 7) + "     "
                    + result.getNewRef().localName() + " -> " + result.getNewRef().getName();
            console.println(line);
        }
    }
}
