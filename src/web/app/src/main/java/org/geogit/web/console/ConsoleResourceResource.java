/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.web.console;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.geogit.api.Context;
import org.geogit.api.GeoGIT;
import org.geogit.api.Platform;
import org.geogit.api.porcelain.ConfigGet;
import org.geogit.cli.ArgumentTokenizer;
import org.geogit.cli.GeogitCLI;
import org.geogit.rest.repository.RESTUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Resource;
import org.restlet.resource.StreamRepresentation;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.google.common.io.FileBackedOutputStream;
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
        checkArgument("run-command".equals(resource), "Invalid entry point. Expected: run-command.");
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
        // not used, we're getting the whole command and args in the "method" object
        // JsonArray paramsArray = json.get("params").getAsJsonArray();

        InputStream in = new ByteArrayInputStream(new byte[0]);
        // dumps output to a temp file if > threshold
        FileBackedOutputStream out = new FileBackedOutputStream(4096);
        try {
            GeoGIT geogit = providedGeogit.get();
            // pass it a BufferedOutputStream 'cause it doesn't buffer the internal FileOutputStream
            ConsoleReader console = new ConsoleReader(in, new BufferedOutputStream(out),
                    new UnsupportedTerminal());
            Platform platform = geogit.getPlatform();

            GeogitCLI geogitCLI = new GeogitCLI(geogit, console);
            geogitCLI.setPlatform(platform);
            geogitCLI.disableProgressListener();

            String[] args = ArgumentTokenizer.tokenize(command);
            final int exitCode = geogitCLI.execute(args);
            JsonObject response = new JsonObject();
            response.addProperty("id", queryId);

            final int charCountLimit = getOutputLimit(geogit.getContext());
            final StringBuilder output = getLimitedOutput(out, charCountLimit);

            if (exitCode == 0) {
                response.addProperty("result", output.toString());
                response.addProperty("error", (String) null);
            } else {
                Exception exception = geogitCLI.exception;
                JsonObject error = buildError(exitCode, output, exception);
                response.add("error", error);
            }

            getResponse().setEntity(response.toString(), MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            // delete temp file
            try {
                out.reset();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    private int getOutputLimit(Context ctx) {
        final int defaultLimit = 1024 * 16;

        Optional<String> configuredLimit = ctx.command(ConfigGet.class)
                .setName("web.console.limit").call();
        int limit = defaultLimit;
        if (configuredLimit.isPresent()) {
            try {
                limit = Integer.parseInt(configuredLimit.get());
            } catch (NumberFormatException ignore) {
                //
                limit = defaultLimit;
            }
            if (limit < 1024) {
                limit = 1024;
            }
        }
        return limit;
    }

    private StringBuilder getLimitedOutput(FileBackedOutputStream out, final int limit)
            throws IOException {

        CharSource charSource = out.asByteSource().asCharSource(Charsets.UTF_8);
        BufferedReader reader = charSource.openBufferedStream();
        final StringBuilder output = new StringBuilder();
        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append('\n');
            count += line.length();
            if (count >= limit) {
                output.append("\nNote: output limited to ")
                        .append(count)
                        .append(" characters. Run config web.console.limit <newlimit> to change the current ")
                        .append(limit).append(" soft limit.");
                break;
            }
        }
        return output;
    }

    private JsonObject buildError(final int exitCode, final StringBuilder output,
            Exception exception) {

        JsonObject error = new JsonObject();
        error.addProperty("code", Integer.valueOf(exitCode));

        if (output.length() == 0 && exception != null && exception.getMessage() != null) {
            output.append(exception.getMessage());
        }
        String message = output.toString();
        error.addProperty("message", message);
        return error;
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
