/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.rest.repository;

import static org.locationtech.geogig.rest.repository.RESTUtils.getGeogit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.locationtech.geogig.api.GeoGIG;
import org.locationtech.geogig.api.ObjectId;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 *
 */
public class ParentResource extends Resource {

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        List<Variant> variants = getVariants();
        variants.add(new ParentRepresentation(request));
    }

    private static class ParentRepresentation extends OutputRepresentation {

        private Request request;

        public ParentRepresentation(Request request) {
            super(MediaType.TEXT_PLAIN);
            this.request = request;
        }

        @Override
        public void write(OutputStream out) throws IOException {
            PrintWriter w = new PrintWriter(out);
            Form options = request.getResourceRef().getQueryAsForm();

            Optional<String> commit = Optional
                    .fromNullable(options.getFirstValue("commitId", null));

            Optional<GeoGIG> geogit = getGeogit(request);
            Preconditions.checkState(geogit.isPresent());
            GeoGIG ggit = geogit.get();

            if (commit.isPresent()) {
                ImmutableList<ObjectId> parents = ggit.getRepository().graphDatabase()
                        .getParents(ObjectId.valueOf(commit.get()));
                for (ObjectId object : parents) {
                    w.write(object.toString() + "\n");
                }
            }
            w.flush();

        }

    }
}
