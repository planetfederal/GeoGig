/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.memory;

/**
 * Edge class used by {@link Graph}
 * 
 * @author Justin Deoliveira, Boundless
 */
class Edge {

    final Node src;
    final Node dst;

    /**
     * Creates a new edge between two nodes.
     */
    Edge(Node src, Node dst) {
        this.src = src;
        this.dst = dst;
    }
}
