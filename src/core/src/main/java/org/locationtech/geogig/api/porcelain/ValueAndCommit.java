/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

import org.locationtech.geogig.api.RevCommit;

import com.google.common.base.Optional;

public class ValueAndCommit {

    public Optional<?> value;

    public RevCommit commit;

    public ValueAndCommit(Optional<?> value, RevCommit commit) {
        this.value = value;
        this.commit = commit;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(value.orNull()).append('/').append(commit.getId())
                .toString();
    }
}
