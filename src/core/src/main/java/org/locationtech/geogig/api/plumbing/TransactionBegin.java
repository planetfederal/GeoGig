/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.locationtech.geogig.api.AbstractGeoGitOp;
import org.locationtech.geogig.api.GeogitTransaction;
import org.locationtech.geogig.api.hooks.Hookable;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Creates a new {@link GeogitTransaction} and copies all of the repository refs for that
 * transaction to use.
 * 
 * @see GeogitTransaction
 */
@Hookable(name = "transaction-start")
public class TransactionBegin extends AbstractGeoGitOp<GeogitTransaction> {

    /**
     * Creates a new transaction and returns it.
     * 
     * @return the {@link GeogitTransaction} that was created by the operation
     */
    @Override
    protected GeogitTransaction _call() {
        Preconditions.checkState(!(context instanceof GeogitTransaction),
                "Cannot start a new transaction within a transaction!");

        GeogitTransaction t = new GeogitTransaction(context, UUID.randomUUID());

        // Lock the repository
        try {
            refDatabase().lock();
        } catch (TimeoutException e) {
            Throwables.propagate(e);
        }
        try {
            // Copy original refs
            t.create();
        } finally {
            // Unlock the repository
            refDatabase().unlock();
        }
        // Return the transaction
        return t;
    }
}
