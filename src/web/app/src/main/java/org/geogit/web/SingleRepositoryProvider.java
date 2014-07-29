/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.web;

import org.geogit.api.GeoGIT;
import org.geogit.rest.repository.RepositoryProvider;
import org.restlet.data.Request;

import com.google.common.base.Optional;

public class SingleRepositoryProvider implements RepositoryProvider {

    private GeoGIT geogit;

    public SingleRepositoryProvider(GeoGIT geogit) {
        this.geogit = geogit;
    }

    @Override
    public Optional<GeoGIT> getGeogit(Request request) {
        return Optional.fromNullable(geogit);
    }

}
