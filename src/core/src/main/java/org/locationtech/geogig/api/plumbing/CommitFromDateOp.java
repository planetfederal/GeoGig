/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing;

import java.util.Date;
import java.util.Iterator;

import org.locationtech.geogig.api.AbstractGeoGigOp;
import org.locationtech.geogig.api.RevCommit;
import org.locationtech.geogig.api.porcelain.LogOp;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Returns the last commit in the current branch at a given date
 */
public class CommitFromDateOp extends AbstractGeoGigOp<Optional<RevCommit>> {

    private Date date;

    public CommitFromDateOp setDate(Date date) {
        this.date = date;
        return this;
    }

    @Override
    protected  Optional<RevCommit> _call() {
        Preconditions.checkState(date != null);
        long time = date.getTime();
        Iterator<RevCommit> iter = command(LogOp.class).setFirstParentOnly(true).call();
        while (iter.hasNext()) {
            RevCommit commit = iter.next();
            if (commit.getCommitter().getTimestamp() < time) {
                return Optional.of(commit);
            }
        }
        return Optional.absent();
    }

}
