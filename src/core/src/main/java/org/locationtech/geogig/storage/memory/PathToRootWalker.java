/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.memory;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Walks a path from the specified node to a root, bifurcating along the way in cases where a node
 * has multiple parents. 
 *
 * @author Justin Deoliveira, Boundless
 *
 */
public class PathToRootWalker implements Iterator<List<Node>> {

    /**
     * node queue
     */
    Queue<Node> q;

    /**
     * visited nodes
     */
    Set<Node> seen;

    public PathToRootWalker(Node start) {
        q = Lists.newLinkedList();
        q.add(start);

        seen = Sets.newHashSet();
    }

    @Override
    public boolean hasNext() {
        return !q.isEmpty();
    }

    @Override
    public List<Node> next() {
        List<Node> curr = Lists.newArrayList();
        List<Node> next = Lists.newArrayList();

        while (!q.isEmpty()) {
            Node node = q.poll();
            curr.add(node);

            Iterables.addAll(next, node.to());
        }

        seen.addAll(curr);
        q.addAll(next);
        return curr;
    }

    public boolean seen(Node node) {
        return seen.contains(node);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
