/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api;

import com.vividsolutions.jts.geom.Envelope;

/**
 *
 */
public interface Bounded {

    public boolean intersects(Envelope env);

    public void expand(Envelope env);
}
