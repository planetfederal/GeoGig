/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.repository;

import java.io.Closeable;
import java.util.Iterator;

import org.geogit.api.Node;
import org.geogit.api.RevTree;
import org.geogit.api.RevTreeBuilder;
import org.geogit.storage.NodePathStorageOrder;
import org.geogit.storage.NodeStorageOrder;

/**
 * Represents a temporary storage of {@link Node} instances to assist {@link RevTreeBuilder2} in
 * creating large {@link RevTree} instances by first building an index of Nodes and then adding all
 * nodes to the {@link RevTreeBuilder} in {@link NodePathStorageOrder}'s order.
 */
interface NodeIndex extends Closeable {

    /**
     * Adds a tree node to the index.
     */
    public abstract void add(Node node);

    /**
     * @return the list of added nodes sorted according to the {@link NodeStorageOrder} comparator.
     */
    public abstract Iterator<Node> nodes();

    /**
     * Closes and releases any resource used by this index. This method is idempotent.
     */
    @Override
    public abstract void close();
}