/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */

package org.geogit.rest.repository;

import static org.geogit.rest.repository.RESTUtils.getGeogit;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.geogit.api.GeoGIT;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;
import org.geogit.repository.Repository;
import org.geogit.storage.ObjectSerializingFactory;
import org.geogit.storage.datastream.DataStreamSerializationFactoryV1;
import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Expects an {@code id} request attribute containing the string representation of an
 * {@link ObjectId} (40 char hex string) to look up for in the repository database and return it as
 * a plain byte stream.
 */
public class ObjectFinder extends Finder {

    @Override
    public Resource findTarget(Request request, Response response) {

        if (request.getAttributes().containsKey("id")) {
            final Optional<GeoGIT> ggit = getGeogit(request);
            Preconditions.checkState(ggit.isPresent());

            final String id = (String) request.getAttributes().get("id");
            final ObjectId oid = ObjectId.valueOf(id);

            GeoGIT geogit = ggit.get();
            Repository repository = geogit.getRepository();
            boolean blobExists = repository.blobExists(oid);
            if (blobExists) {
                ObjectResource objectResource = new ObjectResource(oid, geogit);
                objectResource.init(getContext(), request, response);
                return objectResource;
            }
        }

        return super.findTarget(request, response);
    }

    private static class ObjectResource extends Resource {

        private ObjectId oid;

        private GeoGIT geogit;

        public ObjectResource(ObjectId oid, GeoGIT geogit) {
            this.oid = oid;
            this.geogit = geogit;
        }

        @Override
        public void init(Context context, Request request, Response response) {
            super.init(context, request, response);
            List<Variant> variants = getVariants();

            variants.add(new RevObjectBinaryRepresentation(oid, geogit));
        }
    }

    private static class RevObjectBinaryRepresentation extends OutputRepresentation {
        private final ObjectId oid;

        private static final ObjectSerializingFactory serialFac = DataStreamSerializationFactoryV1.INSTANCE;

        private final GeoGIT ggit;

        public RevObjectBinaryRepresentation(ObjectId oid, GeoGIT ggit) {
            super(MediaType.APPLICATION_OCTET_STREAM);
            this.oid = oid;
            this.ggit = ggit;
        }

        @Override
        public void write(OutputStream out) throws IOException {
            Repository repository = ggit.getRepository();
            RevObject rawObject = repository.objectDatabase().get(oid);
            serialFac.createObjectWriter(rawObject.getType()).write(rawObject, out);
        }
    }

}
