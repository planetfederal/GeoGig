/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.osm.internal;

import org.geotools.feature.DecoratingFeature;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

public class MappedFeature extends DecoratingFeature {

    private String path;

    public MappedFeature(String path, Feature feature) {
        super((SimpleFeature) feature);
        this.path = path;
    }

    @Deprecated
    public Feature getFeature() {
        return this;
    }

    public String getPath() {
        return path;
    }

}
