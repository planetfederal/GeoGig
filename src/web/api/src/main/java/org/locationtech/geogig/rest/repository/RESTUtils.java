/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.locationtech.geogig.rest.repository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.locationtech.geogig.api.GeoGIT;
import org.restlet.data.Request;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class RESTUtils {

    public static Optional<GeoGIT> getGeogit(Request request) {
        RepositoryProvider provider = repositoryProvider(request);
        Optional<GeoGIT> geogit = provider.getGeogit(request);
        return geogit;
    }

    public static RepositoryProvider repositoryProvider(Request request) {
        Object provider = request.getAttributes().get(RepositoryProvider.KEY);
        Preconditions.checkNotNull(provider,
                "No RepositoryProvider found in request attributes under the key %s",
                RepositoryProvider.KEY);
        Preconditions.checkState(provider instanceof RepositoryProvider,
                "Request attribute %s is not of type RepositoryProvider: %s",
                RepositoryProvider.KEY, provider.getClass());
        return (RepositoryProvider) provider;
    }

    public static String getStringAttribute(final Request request, final String key) {
        Object value = request.getAttributes().get(key);
        if (value == null) {
            return null;
        }

        try {
            return URLDecoder.decode(value.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }
}
