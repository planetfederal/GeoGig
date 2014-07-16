/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */

package org.locationtech.geogig.rest.repository;

import org.locationtech.geogig.api.GeoGIT;
import org.restlet.data.Request;

import com.google.common.base.Optional;

public interface RepositoryProvider {

    /**
     * Key used too lookup the {@link RepositoryProvider} instance in the
     * {@link Request#getAttributes() request attributes}
     */
    String KEY = "__REPOSITORY_PROVIDER_KEY__";

    public Optional<GeoGIT> getGeogit(Request request);

}