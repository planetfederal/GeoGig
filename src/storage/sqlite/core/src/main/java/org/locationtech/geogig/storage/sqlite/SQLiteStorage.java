/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.sqlite;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.api.plumbing.ResolveGeogitDir;

import com.google.common.base.Optional;

/**
 * Utility class for SQLite storage.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class SQLiteStorage {
    /**
     * Format name used for configuration.
     */
    public static final String FORMAT_NAME = "sqlite";

    /**
     * Implementation version.
     */
    public static final String VERSION = "0.1";

    /**
     * Returns the .geogit directory for the platform object.
     */
    public static File geogitDir(Platform platform) {
        Optional<URL> url = new ResolveGeogitDir(platform).call();
        if (!url.isPresent()) {
            throw new RuntimeException("Unable to resolve .geogit directory");
        }
        try {
            return new File(url.get().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error resolving .geogit directory", e);
        }
    }
}
