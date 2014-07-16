/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.web.api.commands;

import org.locationtech.geogig.api.GeoGIT;
import org.locationtech.geogig.api.GeogitTransaction;
import org.locationtech.geogig.api.plumbing.TransactionBegin;
import org.locationtech.geogig.web.api.AbstractWebAPICommand;
import org.locationtech.geogig.web.api.CommandContext;
import org.locationtech.geogig.web.api.CommandResponse;
import org.locationtech.geogig.web.api.CommandSpecException;
import org.locationtech.geogig.web.api.ResponseWriter;

/**
 * The interface for the TransactionBegin operation in GeoGit.
 * 
 * Web interface for {@link TransactionBegin}
 */

public class BeginTransaction extends AbstractWebAPICommand {

    /**
     * Runs the command and builds the appropriate response.
     * 
     * @param context - the context to use for this command
     * 
     * @throws CommandSpecException
     */
    @Override
    public void run(CommandContext context) {
        if (this.getTransactionId() != null) {
            throw new CommandSpecException("Tried to start a transaction within a transaction.");
        }
        final GeoGIT geogit = context.getGeoGIT();

        final GeogitTransaction transaction = geogit.command(TransactionBegin.class).call();

        context.setResponseContent(new CommandResponse() {

            @Override
            public void write(ResponseWriter out) throws Exception {
                out.start();
                out.writeTransactionId(transaction.getTransactionId());
                out.finish();
            }
        });
    }

}
