/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.web.api.commands;

import java.io.IOException;
import java.util.List;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.GlobalContextBuilder;
import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.Remote;
import org.locationtech.geogig.api.porcelain.RemoteAddOp;
import org.locationtech.geogig.api.porcelain.RemoteException;
import org.locationtech.geogig.api.porcelain.RemoteListOp;
import org.locationtech.geogig.api.porcelain.RemoteRemoveOp;
import org.locationtech.geogig.api.porcelain.RemoteResolve;
import org.locationtech.geogig.remote.IRemoteRepo;
import org.locationtech.geogig.remote.RemoteUtils;
import org.locationtech.geogig.web.api.AbstractWebAPICommand;
import org.locationtech.geogig.web.api.CommandContext;
import org.locationtech.geogig.web.api.CommandResponse;
import org.locationtech.geogig.web.api.CommandSpecException;
import org.locationtech.geogig.web.api.ResponseWriter;

import com.google.common.base.Optional;

/**
 * Interface for the Remote operations in GeoGit.
 * 
 * Web interface for {@link RemoteListOp}, {@link RemoteRemoveOp}, {@link RemoteAddOp}
 */

public class RemoteWebOp extends AbstractWebAPICommand {

    private boolean list;

    private boolean remove;

    private boolean ping;

    private boolean update;

    private boolean verbose;

    private String remoteName;

    private String newName;

    private String remoteURL;

    private String username = null;

    private String password = null;

    /**
     * Mutator for the list variable
     * 
     * @param list - true to list the names of your remotes
     */
    public void setList(boolean list) {
        this.list = list;
    }

    /**
     * Mutator for the remove variable
     * 
     * @param remove - true to remove the given remote
     */
    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    /**
     * Mutator for the ping variable
     * 
     * @param ping - true to ping the given remote
     */
    public void setPing(boolean ping) {
        this.ping = ping;
    }

    /**
     * Mutator for the update variable
     * 
     * @param update - true to update the given remote
     */
    public void setUpdate(boolean update) {
        this.update = update;
    }

    /**
     * Mutator for the verbose variable
     * 
     * @param update - true to show more info for each repo
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Mutator for the remoteName variable
     * 
     * @param remoteName - the name of the remote to add or remove
     */
    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    /**
     * Mutator for the newName variable
     * 
     * @param newName - the new name of the remote to update
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }

    /**
     * Mutator for the remoteURL variable
     * 
     * @param remoteURL - the URL to the repo to make a remote
     */
    public void setRemoteURL(String remoteURL) {
        this.remoteURL = remoteURL;
    }

    /**
     * Mutator for the username variable
     * 
     * @param username - the username to access the remote
     */
    public void setUserName(String username) {
        this.username = username;
    }

    /**
     * Mutator for the password variable
     * 
     * @param password - the password to access the remote
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Runs the command and builds the appropriate response.
     * 
     * @param context - the context to use for this command
     */
    @Override
    public void run(CommandContext context) {
        final Context geogit = this.getCommandLocator(context);
        if (list) {
            final List<Remote> remotes = geogit.command(RemoteListOp.class).call();

            context.setResponseContent(new CommandResponse() {
                @Override
                public void write(ResponseWriter out) throws Exception {
                    out.start();
                    out.writeRemoteListResponse(remotes, verbose);
                    out.finish();
                }
            });
        } else if (ping) {
            Optional<Remote> remote = geogit.command(RemoteResolve.class).setName(remoteName)
                    .call();

            if (remote.isPresent()) {
                Optional<IRemoteRepo> remoteRepo = RemoteUtils.newRemote(
                        GlobalContextBuilder.builder.build(), remote.get(), null, null);
                if (remoteRepo.isPresent()) {
                    try {
                        remoteRepo.get().open();
                        Ref ref = remoteRepo.get().headRef();
                        remoteRepo.get().close();

                        if (ref != null) {
                            context.setResponseContent(new CommandResponse() {
                                @Override
                                public void write(ResponseWriter out) throws Exception {
                                    out.start();
                                    out.writeRemotePingResponse(true);
                                    out.finish();
                                }
                            });
                        }
                        return;
                    } catch (IOException e) {
                        // Do nothing, we will write the response later.
                    }
                }
            }
            context.setResponseContent(new CommandResponse() {
                @Override
                public void write(ResponseWriter out) throws Exception {
                    out.start();
                    out.writeRemotePingResponse(false);
                    out.finish();
                }
            });
        } else if (remove) {
            if (remoteName == null || remoteName.trim().isEmpty()) {
                throw new CommandSpecException("No remote was specified.");
            }
            final Remote remote;
            try {
                remote = geogit.command(RemoteRemoveOp.class).setName(remoteName).call();
            } catch (RemoteException e) {
                context.setResponseContent(CommandResponse.error(e.statusCode.toString()));
                return;
            } catch (Exception e) {
                context.setResponseContent(CommandResponse.error("Aborting Remote Remove"));
                return;
            }
            context.setResponseContent(new CommandResponse() {
                @Override
                public void write(ResponseWriter out) throws Exception {
                    out.start();
                    out.writeElement("name", remote.getName());
                    out.finish();
                }
            });
        } else if (update) {
            if (remoteName == null || remoteName.trim().isEmpty()) {
                throw new CommandSpecException("No remote was specified.");
            } else if (remoteURL == null || remoteURL.trim().isEmpty()) {
                throw new CommandSpecException("No URL was specified.");
            }
            final Remote newRemote;
            try {
                if (newName != null && !newName.trim().isEmpty() && !newName.equals(remoteName)) {
                    newRemote = geogit.command(RemoteAddOp.class).setName(newName)
                            .setURL(remoteURL).setUserName(username).setPassword(password).call();
                    geogit.command(RemoteRemoveOp.class).setName(remoteName).call();
                } else {
                    geogit.command(RemoteRemoveOp.class).setName(remoteName).call();
                    newRemote = geogit.command(RemoteAddOp.class).setName(remoteName)
                            .setURL(remoteURL).setUserName(username).setPassword(password).call();
                }
            } catch (RemoteException e) {
                context.setResponseContent(CommandResponse.error(e.statusCode.toString()));
                return;
            } catch (Exception e) {
                context.setResponseContent(CommandResponse.error("Aborting Remote Update"));
                return;
            }
            context.setResponseContent(new CommandResponse() {
                @Override
                public void write(ResponseWriter out) throws Exception {
                    out.start();
                    out.writeElement("name", newRemote.getName());
                    out.finish();
                }
            });
        } else {
            if (remoteName == null || remoteName.trim().isEmpty()) {
                throw new CommandSpecException("No remote was specified.");
            } else if (remoteURL == null || remoteURL.trim().isEmpty()) {
                throw new CommandSpecException("No URL was specified.");
            }
            final Remote remote;
            try {
                remote = geogit.command(RemoteAddOp.class).setName(remoteName).setURL(remoteURL)
                        .setUserName(username).setPassword(password).call();
            } catch (RemoteException e) {
                context.setResponseContent(CommandResponse.error(e.statusCode.toString()));
                return;
            } catch (Exception e) {
                context.setResponseContent(CommandResponse.error("Aborting Remote Add"));
                return;
            }
            context.setResponseContent(new CommandResponse() {
                @Override
                public void write(ResponseWriter out) throws Exception {
                    out.start();
                    out.writeElement("name", remote.getName());
                    out.finish();
                }
            });
        }
    }

}
