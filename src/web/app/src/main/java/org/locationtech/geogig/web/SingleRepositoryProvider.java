/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.locationtech.geogig.web;

import org.locationtech.geogig.api.GeoGIG;
import org.locationtech.geogig.rest.repository.RepositoryProvider;
import org.restlet.data.Request;

import com.google.common.base.Optional;

public class SingleRepositoryProvider implements RepositoryProvider {

    private GeoGIG geogig;

    public SingleRepositoryProvider(GeoGIG geogig) {
        this.geogig = geogig;
    }

    @Override
    public Optional<GeoGIG> getGeogig(Request request) {
        return Optional.fromNullable(geogig);
    }

}
