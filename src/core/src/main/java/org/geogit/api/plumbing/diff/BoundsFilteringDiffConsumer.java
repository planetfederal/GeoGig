/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing.diff;

import org.geogit.api.Bucket;
import org.geogit.api.Node;
import org.geogit.api.plumbing.diff.DiffTreeVisitor.Consumer;
import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * A {@link Consumer} decorator that filters {@link Node nodes} by a bounding box intersection check
 * before delegating.
 */
public class BoundsFilteringDiffConsumer extends DiffTreeVisitor.ForwardingConsumer {

    private ReferencedEnvelope bounds;

    public BoundsFilteringDiffConsumer(ReferencedEnvelope bounds, DiffTreeVisitor.Consumer delegate) {
        super(delegate);
        this.bounds = bounds;
    }

    @Override
    public boolean tree(Node left, Node right) {

        return super.tree(left, right);
    }

    @Override
    public void endTree(Node left, Node right) {
        super.endTree(left, right);
    }

    @Override
    public boolean bucket(int bucketIndex, int bucketDepth, Bucket left, Bucket right) {

        return super.bucket(bucketIndex, bucketDepth, left, right);
    }

    @Override
    public void feature(Node left, Node right) {
        super.feature(left, right);
    }

}
