/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */

package org.locationtech.geogig.rest.repository;

import static org.locationtech.geogig.rest.repository.RESTUtils.getGeogit;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.locationtech.geogig.api.GeoGIT;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.repository.Repository;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 *
 */
public class ObjectExistsResource extends Resource {

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        List<Variant> variants = getVariants();

        variants.add(new ObjectExistsRepresentation());
    }

    private class ObjectExistsRepresentation extends WriterRepresentation {
        public ObjectExistsRepresentation() {
            super(MediaType.TEXT_PLAIN);
        }

        @Override
        public void write(Writer w) throws IOException {
            Form options = getRequest().getResourceRef().getQueryAsForm();

            ObjectId oid = ObjectId.valueOf(options.getFirstValue("oid", ObjectId.NULL.toString()));
            Request request = getRequest();
            Optional<GeoGIT> ggit = getGeogit(request);
            Preconditions.checkState(ggit.isPresent());

            GeoGIT geogit = ggit.get();
            Repository repository = geogit.getRepository();
            boolean blobExists = repository.blobExists(oid);

            if (blobExists) {
                w.write("1");
            } else {
                w.write("0");
            }
            w.flush();
        }
    }
}
