/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.annotation.Nullable;

import org.locationtech.geogig.api.AbstractGeoGigOp;
import org.locationtech.geogig.api.Platform;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Resolves the location of the {@code .geogit} repository directory relative to the
 * {@link Platform#pwd() current directory}.
 * <p>
 * The location can be a either the current directory, a parent of it, or {@code null} if no
 * {@code .geogit} directory is found.
 * 
 */
public class ResolveGeogigDir extends AbstractGeoGigOp<Optional<URL>> {

    private Platform platform;

    public ResolveGeogigDir() {
        //
    }

    public ResolveGeogigDir(Platform platform) {
        this.platform = platform;
    }

    public static Optional<URL> lookup(final File directory) {
        try {
            return Optional.fromNullable(lookupGeogitDirectory(directory));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected Platform platform() {
        return this.platform == null ? super.platform() : this.platform;
    }

    /**
     * @return the location of the {@code .geogit} repository environment directory or {@code null}
     *         if not inside a working directory
     * @see org.locationtech.geogig.api.AbstractGeoGigOp#call()
     */
    @Override
    protected Optional<URL> _call() {
        File pwd = platform().pwd();
        Optional<URL> repoLocation = ResolveGeogigDir.lookup(pwd);
        return repoLocation;
    }

    public Optional<File> getFile() {
        Optional<URL> url = call();
        if (url.isPresent()) {
            try {
                if ("file".equalsIgnoreCase(url.get().getProtocol())) {
                    return Optional.of(new File(url.get().toURI()));
                }
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        return Optional.absent();
    }

    /**
     * @param file the directory to search
     * @return the location of the {@code .geogit} repository environment directory or {@code null}
     *         if not inside a working directory
     */
    private static URL lookupGeogitDirectory(@Nullable File file) throws IOException {
        if (file == null) {
            return null;
        }
        if (file.isDirectory()) {
            if (file.getName().equals(".geogit")) {
                return file.toURI().toURL();
            }
            File[] contents = file.listFiles();
            Preconditions.checkNotNull(contents,
                    "Either '%s' is not a directory or an I/O error ocurred listing its contents",
                    file.getAbsolutePath());
            for (File dir : contents) {
                if (dir.isDirectory() && dir.getName().equals(".geogit")) {
                    return lookupGeogitDirectory(dir);
                }
            }
        }
        return lookupGeogitDirectory(file.getParentFile());
    }

}
