/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.rest.repository;

import org.geogit.api.GeoGIT;
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