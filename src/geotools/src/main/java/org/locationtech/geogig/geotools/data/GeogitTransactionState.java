/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.data;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;
import org.geotools.data.store.ContentEntry;
import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.GeogitTransaction;
import org.locationtech.geogig.api.plumbing.DiffIndex;
import org.locationtech.geogig.api.plumbing.TransactionBegin;
import org.locationtech.geogig.api.plumbing.diff.DiffEntry;
import org.locationtech.geogig.api.porcelain.AddOp;
import org.locationtech.geogig.api.porcelain.CheckoutOp;
import org.locationtech.geogig.api.porcelain.CommitOp;
import org.locationtech.geogig.api.porcelain.ConflictsException;
import org.locationtech.geogig.api.porcelain.NothingToCommitException;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 *
 */
class GeogitTransactionState implements State {

    /** VERSIONING_COMMIT_AUTHOR */
    static final String VERSIONING_COMMIT_AUTHOR = "VersioningCommitAuthor";

    /** VERSIONING_COMMIT_MESSAGE */
    static final String VERSIONING_COMMIT_MESSAGE = "VersioningCommitMessage";

    private ContentEntry entry;

    private GeogitTransaction geogitTx;

    private Transaction tx;

    /**
     * @param entry
     */
    public GeogitTransactionState(ContentEntry entry) {
        this.entry = entry;
    }

    public Optional<GeogitTransaction> getGeogitTransaction() {
        return Optional.fromNullable(this.geogitTx);
    }

    @Override
    public void setTransaction(@Nullable final Transaction transaction) {
        Preconditions.checkArgument(!Transaction.AUTO_COMMIT.equals(transaction));

        if (transaction != null && this.tx != null) {
            throw new IllegalStateException(
                    "New transaction set without closing old transaction first.");
        }
        this.tx = transaction;

        if (transaction == null) {
            // Transaction.removeState has been called (during
            // transaction.close())
            if (this.geogitTx != null) {
                // throw new
                // IllegalStateException("Transaction is attempting to "
                // + "close a non committed or aborted geogit transaction");
                geogitTx.abort();
            }
            this.geogitTx = null;
        } else {
            if (this.geogitTx != null) {
                geogitTx.abort();
            }
            GeoGitDataStore dataStore = (GeoGitDataStore) entry.getDataStore();
            Context commandLocator = dataStore.getCommandLocator(this.tx);
            this.geogitTx = commandLocator.command(TransactionBegin.class).call();
            // checkout the working branch
            final String workingBranch = dataStore.getOrFigureOutBranch();
            this.geogitTx.command(CheckoutOp.class).setForce(true).setSource(workingBranch).call();
        }
    }

    @Override
    public void addAuthorization(String AuthID) throws IOException {
        // not required
    }

    @Override
    public void commit() throws IOException {
        Preconditions.checkState(this.geogitTx != null);
        /*
         * This follows suite with the hack set on GeoSever's
         * org.geoserver.wfs.Transaction.getDatastoreTransaction()
         */
        final Optional<String> txUserName = getTransactionProperty(VERSIONING_COMMIT_AUTHOR);
        final Optional<String> fullName = getTransactionProperty("fullname");
        final Optional<String> email = getTransactionProperty("email");

        final String author = fullName.isPresent() ? fullName.get() : txUserName.orNull();
        String commitMessage = getTransactionProperty(VERSIONING_COMMIT_MESSAGE).orNull();

        this.geogitTx.command(AddOp.class).call();
        try {
            CommitOp commitOp = this.geogitTx.command(CommitOp.class);
            if (txUserName != null) {
                commitOp.setAuthor(author, email.orNull());
            }
            if (commitMessage == null) {
                commitMessage = composeDefaultCommitMessage();
            }
            commitOp.setMessage(commitMessage);
            commitOp.call();
        } catch (NothingToCommitException nochanges) {
            // ok
        }

        try {
            this.geogitTx.setAuthor(author, email.orNull()).commit();
        } catch (ConflictsException e) {
            // TODO: how should this be handled?
            this.geogitTx.abort();
        }

        this.geogitTx = null;
    }

    private Optional<String> getTransactionProperty(final String propName) {
        Object property = this.tx.getProperty(propName);
        if (property instanceof String) {
            return Optional.of((String) property);
        }
        return Optional.absent();
    }

    private String composeDefaultCommitMessage() {
        Iterator<DiffEntry> indexDiffs = this.geogitTx.command(DiffIndex.class).call();
        int added = 0, removed = 0, modified = 0;
        StringBuilder msg = new StringBuilder();
        while (indexDiffs.hasNext()) {
            DiffEntry entry = indexDiffs.next();
            switch (entry.changeType()) {
            case ADDED:
                added++;
                break;
            case MODIFIED:
                modified++;
                break;
            case REMOVED:
                removed++;
                break;
            }
            if ((added + removed + modified) < 10) {
                msg.append("\n ").append(entry.changeType().toString().toLowerCase()).append(' ')
                        .append(entry.newPath() == null ? entry.oldName() : entry.newPath());
            }
        }
        int count = added + removed + modified;
        if (count > 10) {
            msg.append("\n And ").append(count - 10).append(" more changes.");
        }
        StringBuilder title = new StringBuilder();
        if (added > 0) {
            title.append("added ").append(added);
        }
        if (modified > 0) {
            if (title.length() > 0) {
                title.append(", ");
            }
            title.append("modified ").append(modified);
        }
        if (removed > 0) {
            if (title.length() > 0) {
                title.append(", ");
            }
            title.append("removed ").append(removed);
        }
        if (count > 0) {
            title.append(" features via unversioned legacy client.\n");
        }
        msg.insert(0, title);
        return msg.toString();
    }

    @Override
    public void rollback() throws IOException {
        Preconditions.checkState(this.geogitTx != null);
        this.geogitTx.abort();
        this.geogitTx = null;
    }

}