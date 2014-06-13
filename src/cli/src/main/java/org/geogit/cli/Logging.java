/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.cli;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Nullable;

import org.geogit.api.DefaultPlatform;
import org.geogit.api.plumbing.ResolveGeogitDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

/**
 * Utility class for the CLI applications to configure logging using the default logback logger
 * context.
 * <p>
 * {@link #tryConfigureLogging()} is meant to be called by the {@code static main(String [])}
 * methods or such places where it wouldn't interfere with any alternate logging mechanism a client
 * application may be using (e.g. geoserver using log4j instead).
 */
class Logging {
    private static final Logger LOGGER = LoggerFactory.getLogger(Logging.class);

    private static File geogitDirLoggingConfiguration;

    static void tryConfigureLogging() {
        // instantiate and call ResolveGeogitDir directly to avoid calling getGeogit() and hence get
        // some logging events before having configured logging
        final Optional<URL> geogitDirUrl = new ResolveGeogitDir(new DefaultPlatform()).call();
        if (!geogitDirUrl.isPresent() || !"file".equalsIgnoreCase(geogitDirUrl.get().getProtocol())) {
            // redirect java.util.logging to SLF4J anyways
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            return;
        }

        final File geogitDir;
        try {
            geogitDir = new File(geogitDirUrl.get().toURI());
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }

        if (geogitDir.equals(geogitDirLoggingConfiguration)) {
            return;
        }

        if (!geogitDir.exists() || !geogitDir.isDirectory()) {
            return;
        }
        final URL loggingFile = getOrCreateLoggingConfigFile(geogitDir);

        if (loggingFile == null) {
            return;
        }

        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.reset();
            /*
             * Set the geogitdir variable for the config file can resolve the default location
             * ${geogitdir}/log/geogit.log
             */
            loggerContext.putProperty("geogitdir", geogitDir.getAbsolutePath());
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            configurator.doConfigure(loggingFile);

            // redirect java.util.logging to SLF4J
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            geogitDirLoggingConfiguration = geogitDir;
        } catch (JoranException e) {
            LOGGER.error("Error configuring logging from file {}. '{}'", loggingFile,
                    e.getMessage(), e);
        }
    }

    @Nullable
    private static URL getOrCreateLoggingConfigFile(final File geogitdir) {

        final File logsDir = new File(geogitdir, "log");
        if (!logsDir.exists() && !logsDir.mkdir()) {
            return null;
        }
        final File configFile = new File(logsDir, "logback.xml");
        if (configFile.exists()) {
            try {
                return configFile.toURI().toURL();
            } catch (MalformedURLException e) {
                throw Throwables.propagate(e);
            }
        }
        ByteSource from;
        final URL resource = GeogitCLI.class.getResource("logback_default.xml");
        try {
            from = Resources.asByteSource(resource);
        } catch (NullPointerException npe) {
            LOGGER.warn("Couldn't obtain default logging configuration file");
            return null;
        }
        try {
            from.copyTo(Files.asByteSink(configFile));
            return configFile.toURI().toURL();
        } catch (Exception e) {
            LOGGER.warn("Error copying logback_default.xml to {}. Using default configuration.",
                    configFile, e);
            return resource;
        }
    }

}
