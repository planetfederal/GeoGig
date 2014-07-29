/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.data;

import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.DecoratingFeatureIterator;
import org.opengis.feature.Feature;

public class ForwardingFeatureIterator<F extends Feature> extends DecoratingFeatureIterator<F> {

    public ForwardingFeatureIterator(FeatureIterator<F> iterator) {
        super(iterator);
    }

}
