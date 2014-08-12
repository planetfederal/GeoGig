/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.remote;

import java.io.Closeable;
import java.io.IOException;

import org.locationtech.geogig.api.RevObject;

/**
 * A closeable funnel used to transparently send objects to a remote resource.
 */
public interface ObjectFunnel extends Closeable {

    public void funnel(RevObject object) throws IOException;
}
