/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.osm.internal.log;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.locationtech.geogig.api.AbstractGeoGitOp;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

/**
 * Adds an entry to the OSM log. The osm is used to keep track of trees created after importing from
 * OSM data, o they can be used as starting points for updating. The log is basically a list of
 * those trees that represent a dataset that correspond to given OSM snapshot and can, therefore, be
 * used to synchronize
 */
public class AddOSMLogEntry extends AbstractGeoGitOp<Void> {

    private OSMLogEntry entry;

    public AddOSMLogEntry setEntry(OSMLogEntry entry) {
        this.entry = entry;
        return this;
    }

    @Override
    protected Void _call() {
        URL file = command(ResolveOSMLogfile.class).call();
        try {
            Files.append(entry.toString() + "\n", new File(file.getFile()), Charsets.UTF_8);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return null;
    }

}
