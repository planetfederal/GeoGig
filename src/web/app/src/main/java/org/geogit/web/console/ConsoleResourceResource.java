/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.web.console;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.geogit.api.GeoGIT;
import org.geogit.api.Platform;
import org.geogit.cli.GeogitCLI;
import org.geogit.rest.repository.RESTUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Resource;
import org.restlet.resource.StreamRepresentation;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 */
public class ConsoleResourceResource extends Resource {

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    /**
     * Handles JSON RPC 2.0 (http://json-rpc.org/wiki/specification) calls to the
     * <code>/console/run-command end point</code>.
     * 
     * <p>
     * Example request body content:
     * <ul>
     * <li> <code>{"jsonrpc":"2.0","method":"status","params":["--help"],"id":3}</code>
     * <li>
     * <code>{"jsonrpc":"2.0","method":"commit","params":["roads","-m","deleted one road"],"id":8}</code>
     * </ul>
     * 
     */
    @Override
    public void handlePost() {
        final Request request = getRequest();
        final String resource = RESTUtils.getStringAttribute(getRequest(), "resource");
        checkArgument("run-command".equals(resource), "naaaahhh");
        JsonParser parser = new JsonParser();
        InputRepresentation entityAsObject = (InputRepresentation) request.getEntity();
        JsonObject json;
        try {
            InputStream stream = entityAsObject.getStream();
            InputStreamReader reader = new InputStreamReader(stream);
            json = (JsonObject) parser.parse(reader);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        Preconditions.checkArgument("2.0".equals(json.get("jsonrpc").getAsString()));
        Optional<GeoGIT> providedGeogit = RESTUtils.getGeogit(request);
        checkArgument(providedGeogit.isPresent());
        final String command = json.get("method").getAsString();
        final String queryId = json.get("id").getAsString();
        JsonArray paramsArray = json.get("params").getAsJsonArray();

        List<String> cmdAndArgs = new ArrayList<String>(1 + paramsArray.size());
        cmdAndArgs.add(command);
        for (Iterator<JsonElement> i = paramsArray.iterator(); i.hasNext();) {
            JsonElement argElem = i.next();
            if (argElem.isJsonNull()
                    || ((argElem instanceof JsonObject) && ((JsonObject) argElem).entrySet()
                            .isEmpty())) {
                continue;
            }
            cmdAndArgs.add(argElem.getAsString());
        }

        InputStream in = new ByteArrayInputStream(new byte[0]);
        OutputStream out = new ByteArrayOutputStream();
        ConsoleReader consoleReader;
        try {
            consoleReader = new ConsoleReader(in, out, new UnsupportedTerminal());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        GeoGIT geogit = providedGeogit.get();
        GeogitCLI geogitCLI = new GeogitCLI(geogit, consoleReader);
        Platform platform = geogit.getPlatform();
        geogitCLI.setPlatform(platform);

        String[] args = cmdAndArgs.toArray(new String[cmdAndArgs.size()]);
        final int exitCode = geogitCLI.execute(args);
        JsonObject response = new JsonObject();
        response.addProperty("id", queryId);
        if (exitCode == 0) {
            final String output = out.toString();
            // w.print("{\"result\": \"Hello JSON-RPC\", \"error\": null, \"id\": 1}");
            response.addProperty("result", output);
            response.addProperty("error", (String) null);
        } else {
            JsonObject error = new JsonObject();
            error.addProperty("code", Integer.valueOf(exitCode));
            Exception exception = geogitCLI.exception;
            error.addProperty("message", geogitCLI.exception == null ? "" : exception.getMessage());
            response.add("error", error);
        }

        getResponse().setEntity(response.toString(), MediaType.APPLICATION_JSON);
    }

    @Override
    public void handleGet() {
        final String resourceName;
        {
            String res = RESTUtils.getStringAttribute(getRequest(), "resource");
            if (null == res) {
                resourceName = "terminal.html";
            } else {
                resourceName = res;
            }
        }
        MediaType mediaType = guessMediaType(resourceName);
        getResponse().setEntity(new StreamRepresentation(mediaType) {

            @Override
            public void write(OutputStream outputStream) throws IOException {
                // System.out.println("returning " + resourceName);
                ByteStreams.copy(getStream(), outputStream);
            }

            @Override
            public InputStream getStream() throws IOException {
                InputStream inputStream = ConsoleResourceResource.class
                        .getResourceAsStream(resourceName);
                return inputStream;
            }
        });
    }

    private MediaType guessMediaType(final String resourceName) {
        final int extIdx = resourceName.lastIndexOf('.');
        final String extension = resourceName.substring(extIdx + 1).toLowerCase();
        if ("js".equals(extension)) {
            return MediaType.APPLICATION_JAVASCRIPT;
        }
        if ("css".equals(extension)) {
            return MediaType.TEXT_CSS;
        }
        if ("html".equals(extension)) {
            return MediaType.TEXT_HTML;
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
