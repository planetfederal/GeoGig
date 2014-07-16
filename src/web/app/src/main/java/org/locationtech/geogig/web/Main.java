/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.web;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.DefaultPlatform;
import org.locationtech.geogig.api.GeoGIG;
import org.locationtech.geogig.api.GlobalContextBuilder;
import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.api.plumbing.ResolveGeogigDir;
import org.locationtech.geogig.cli.CLIContextBuilder;
import org.locationtech.geogig.rest.repository.CommandResource;
import org.locationtech.geogig.rest.repository.FixedEncoder;
import org.locationtech.geogig.rest.repository.RepositoryProvider;
import org.locationtech.geogig.rest.repository.RepositoryRouter;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.application.Decoder;

/**
 * Both an embedded jetty launcher
 */
public class Main extends Application {

    static {
        setup();
    }

    private RepositoryProvider repoProvider;

    public Main() {
        super();
    }

    public Main(GeoGIG geogit) {
        super();
        this.repoProvider = new SingleRepositoryProvider(geogit);
    }

    @Override
    public void setContext(org.restlet.Context context) {
        super.setContext(context);
        assert context != null;

        Map<String, Object> attributes = context.getAttributes();

        GeoGIG geogit;
        if (attributes.containsKey("geogit")) {
            geogit = (GeoGIG) attributes.get("geogit");
        } else {
            // revisit, not used at all
            // ServletContext sc = (ServletContext) dispatcher.getContext()
            // .getAttributes().get("org.restlet.ext.servlet.ServletContext");
            // String repo = sc.getInitParameter("repository");
            String repo = null;
            if (repo == null) {
                repo = System.getProperty("org.locationtech.geogig.web.repository");
            }
            if (repo == null) {
                return;
                // throw new IllegalStateException(
                // "Cannot launch geogit servlet without `repository` parameter");
            }
            geogit = loadGeoGIT(repo);
        }
        repoProvider = new SingleRepositoryProvider(geogit);
    }

    @Override
    public Restlet createRoot() {

        Router router = new Router() {

            @Override
            protected synchronized void init(Request request, Response response) {
                super.init(request, response);
                if (!isStarted()) {
                    return;
                }
                request.getAttributes().put(RepositoryProvider.KEY, repoProvider);
            }
        };
        RepositoryRouter root = new RepositoryRouter();

        router.attach("/repo", root);
        router.attach("/{command}.{extension}", CommandResource.class);
        router.attach("/{command}", CommandResource.class);

        org.restlet.Context context = getContext();
        // enable support for compressing responses if the client supports it.
        // NOTE: restlet 1.0.8 leaves a dangling thread on each request (see
        // EncodeRepresentation.getStream()
        // This problem is fixed in latest versions (2.x) of restlet. See the javadocs for
        // FixedEncoder for further detail
        // Encoder responseEncoder = new com.noelios.restlet.application.Encoder(context);
        FixedEncoder encoder = new FixedEncoder(context);
        encoder.setEncodeRequest(false);
        encoder.setEncodeResponse(true);
        encoder.setNext(router);

        Decoder decoder = new Decoder(context);
        decoder.setDecodeRequest(true);
        decoder.setDecodeResponse(false);
        decoder.setNext(encoder);

        return decoder;
    }

    static GeoGIG loadGeoGIT(String repo) {
        Platform platform = new DefaultPlatform();
        platform.setWorkingDir(new File(repo));
        Context inj = GlobalContextBuilder.builder.build();
        GeoGIG geogit = new GeoGIG(inj, platform.pwd());

        if (geogit.command(ResolveGeogigDir.class).call().isPresent()) {
            geogit.getRepository();
            return geogit;
        }

        return geogit;
    }

    static void startServer(String repo) throws Exception {
        GeoGIG geogit = loadGeoGIT(repo);
        org.restlet.Context context = new org.restlet.Context();
        Application application = new Main(geogit);
        application.setContext(context);
        Component comp = new Component();
        comp.getDefaultHost().attach(application);
        comp.getServers().add(Protocol.HTTP, 8182);
        comp.start();
    }

    static void setup() {
        GlobalContextBuilder.builder = new CLIContextBuilder();
    }

    public static void main(String[] args) throws Exception {
        LinkedList<String> argList = new LinkedList<String>(Arrays.asList(args));
        if (argList.size() == 0) {
            System.out.println("provide geogit repo path");
            System.exit(1);
        }
        String repo = argList.pop();
        startServer(repo);
    }

}
