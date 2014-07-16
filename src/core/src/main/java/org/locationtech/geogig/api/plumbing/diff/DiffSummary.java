/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing.diff;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

/**
 * A tuple representing the result of some computation against the diff between two trees, holding
 * the result of the left and right sides of the computation, and optionally the merged/unioned
 * result.
 * 
 * @param <T>
 */
public class DiffSummary<T, M> {

    private T left;

    private T right;

    private M merged;

    public DiffSummary(T left, T right, @Nullable M merged) {
        this.left = left;
        this.right = right;
        this.merged = merged;
    }

    public T getLeft() {
        return left;
    }

    public T getRight() {
        return right;
    }

    public Optional<M> getMergedResult() {
        return Optional.fromNullable(merged);
    }
}
