/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */

package org.geogit.rest.repository;

import static org.geogit.rest.repository.RESTUtils.getGeogit;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.geogit.api.GeoGIT;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.plumbing.RefParse;
import org.geogit.web.api.commands.PushManager;
import org.restlet.Context;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 *
 */
public class EndPush extends Resource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndPush.class);

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        List<Variant> variants = getVariants();
        variants.add(new EndPushRepresentation());
    }

    private class EndPushRepresentation extends WriterRepresentation {
        public EndPushRepresentation() {
            super(MediaType.TEXT_PLAIN);
        }

        @Override
        public void write(Writer w) throws IOException {
            Request request = getRequest();
            try {
                ClientInfo info = request.getClientInfo();
                Optional<GeoGIT> ggit = getGeogit(request);
                Preconditions.checkState(ggit.isPresent());
                Form options = request.getResourceRef().getQueryAsForm();

                final GeoGIT geogit = ggit.get();

                // make a combined ip address to handle requests from multiple machines in the same
                // external network.
                // e.g.: ext.ern.al.IP.int.ern.al.IP
                String ipAddress = info.getAddress() + "."
                        + options.getFirstValue("internalIp", "");
                LOGGER.debug("Initiating EndPush for '{}'", ipAddress);

                String refspec = options.getFirstValue("refspec", null);
                final ObjectId oid = ObjectId.valueOf(options.getFirstValue("objectId",
                        ObjectId.NULL.toString()));
                final ObjectId originalRefValue = ObjectId.valueOf(options.getFirstValue(
                        "originalRefValue", ObjectId.NULL.toString()));

                Optional<Ref> currentRef = geogit.command(RefParse.class).setName(refspec).call();
                ObjectId currentRefId = currentRef.isPresent() ? currentRef.get().getObjectId()
                        : ObjectId.NULL;
                if (!currentRefId.isNull() && !currentRefId.equals(originalRefValue)) {
                    // Abort push
                    w.write("Push aborted for address: " + ipAddress
                            + ". The ref was changed during push.");
                    w.flush();
                } else {
                    PushManager pushManager = PushManager.get();
                    pushManager.connectionSucceeded(geogit, ipAddress, refspec, oid);
                    w.write("Push succeeded for address: " + ipAddress);
                    w.flush();
                }
            } catch (Exception e) {
                Throwables.propagateIfInstanceOf(e, IOException.class);
                throw Throwables.propagate(e);
            }
        }
    }

}
