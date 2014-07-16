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

    private GeoGIG geogit;

    public SingleRepositoryProvider(GeoGIG geogit) {
        this.geogit = geogit;
    }

    @Override
    public Optional<GeoGIG> getGeogit(Request request) {
        return Optional.fromNullable(geogit);
    }

}
