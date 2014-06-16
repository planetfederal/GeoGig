/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.geotools.cli.porcelain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.geogit.cli.AbstractCommand;
import org.geogit.cli.CLICommand;
import org.geogit.cli.CommandFailedException;
import org.geotools.data.DataStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;

public abstract class AbstractGeoJsonCommand extends AbstractCommand implements CLICommand {

    protected DataStore getDataStore(String geoJSON) throws FileNotFoundException, IOException {
        try {
            File geoJsonfile = new File(geoJSON);
            checkParameter(geoJsonfile.exists(), "File does not exist '%s'", geoJsonfile);
            InputStream in = new FileInputStream(geoJsonfile);
            FeatureJSON fjson = new FeatureJSON();
            @SuppressWarnings("rawtypes")
            FeatureCollection fc = fjson.readFeatureCollection(in);
            @SuppressWarnings("unchecked")
            DataStore dataStore = new MemoryDataStore(fc);
            return dataStore;
        } catch (IOException ioe) {
            throw new CommandFailedException("Error opening GeoJSON: " + ioe.getMessage(), ioe);
        }
    }

}
