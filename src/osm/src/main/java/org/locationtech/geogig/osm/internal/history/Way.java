/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.osm.internal.history;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 *
 */
public class Way extends Primitive {

    private List<Long> nodes;

    public Way() {
        super();
        this.nodes = Lists.newLinkedList();
    }

    /**
     * @param nodeRef
     */
    void addNode(long nodeRef) {
        nodes.add(Long.valueOf(nodeRef));
    }

    public ImmutableList<Long> getNodes() {
        return ImmutableList.copyOf(nodes);
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append(",nodes:").append(nodes).append(']')
                .toString();
    }

}
