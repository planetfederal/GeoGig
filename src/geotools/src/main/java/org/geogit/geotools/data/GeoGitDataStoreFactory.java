/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.geotools.data;

import java.awt.RenderingHints.Key;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

import org.geogit.api.ContextBuilder;
import org.geogit.api.GeoGIT;
import org.geogit.api.GlobalContextBuilder;
import org.geogit.cli.CLIContextBuilder;
import org.geogit.repository.Repository;
import org.geotools.data.DataStoreFactorySpi;

import com.google.common.base.Preconditions;

public class GeoGitDataStoreFactory implements DataStoreFactorySpi {

    /** GEO_GIT */
    public static final String DISPLAY_NAME = "GeoGIT";

    static {
        if (GlobalContextBuilder.builder == null
                || GlobalContextBuilder.builder.getClass().equals(ContextBuilder.class)) {
            GlobalContextBuilder.builder = new CLIContextBuilder();
        }
    }

    public static final Param REPOSITORY = new Param("geogit_repository", File.class,
            "Root directory for the geogit repository", true, "/path/to/repository");

    public static final Param BRANCH = new Param(
            "branch",
            String.class,
            "Optional branch name the DataStore operates against, defaults to the currently checked out branch",
            false);

    public static final Param HEAD = new Param(
            "head",
            String.class,
            "Optional refspec (branch name, commit id, etc.) the DataStore operates against, defaults to the currently checked out branch",
            false);

    public static final Param DEFAULT_NAMESPACE = new Param("namespace", String.class,
            "Optional namespace for feature types that do not declare a Namespace themselves",
            false);

    public static final Param CREATE = new Param("create", Boolean.class,
            "Optional flag to enable creation of a new repository if it does not exist", false);

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescription() {
        return "GeoGIT Versioning DataStore";
    }

    @Override
    public Param[] getParametersInfo() {
        return new Param[] { REPOSITORY, BRANCH, HEAD, DEFAULT_NAMESPACE, CREATE };
    }

    @Override
    public boolean canProcess(Map<String, Serializable> params) {
        try {
            Object repository = REPOSITORY.lookUp(params);

            // check that repository points to a file, and either that fiel is a directory, or the
            // the create option is set
            return repository instanceof File && ((File) repository).isDirectory()
                    || Boolean.TRUE.equals(CREATE.lookUp(params));
        } catch (IOException e) {
            //
        }
        return false;
    }

    /**
     * @see org.geotools.data.DataAccessFactory#isAvailable()
     */
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Map<Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }

    @Override
    public GeoGitDataStore createDataStore(Map<String, Serializable> params) throws IOException {

        final File repositoryRoot = (File) REPOSITORY.lookUp(params);

        @Nullable
        final String defaultNamespace = (String) DEFAULT_NAMESPACE.lookUp(params);

        @Nullable
        final String branch = (String) BRANCH.lookUp(params);

        @Nullable 
        final String head = (String) HEAD.lookUp(params);

        @Nullable
        final String effectiveHead = (head == null) ? branch : head;

        @Nullable
        final Boolean create = (Boolean) CREATE.lookUp(params);

        if (create != null && create.booleanValue()) {
            if (!repositoryRoot.exists()) {
                return createNewDataStore(params);
            }
        }

        GeoGIT geogit;
        try {
            geogit = new GeoGIT(repositoryRoot);
        } catch (RuntimeException e) {
            throw new IOException(e.getMessage(), e);
        }
        Repository repository = geogit.getRepository();
        if (null == repository) {
            if (create != null && create.booleanValue()) {
                return createNewDataStore(params);
            }

            throw new IOException(String.format("Directory is not a geogit repository: '%s'",
                    repositoryRoot.getAbsolutePath()));
        }

        GeoGitDataStore store = new GeoGitDataStore(geogit);
        if (defaultNamespace != null) {
            store.setNamespaceURI(defaultNamespace);
        }
        if (effectiveHead != null) {
            store.setHead(effectiveHead);
        }
        return store;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    @Override
    public GeoGitDataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        String defaultNamespace = (String) DEFAULT_NAMESPACE.lookUp(params);

        File repositoryRoot = (File) REPOSITORY.lookUp(params);
        if (!repositoryRoot.isDirectory()) {
            if (repositoryRoot.exists()) {
                throw new IOException(repositoryRoot.getAbsolutePath() + " is not a directory");
            }
            repositoryRoot.mkdirs();
        }

        GeoGIT geogit = new GeoGIT(repositoryRoot);

        try {
            Repository repository = geogit.getOrCreateRepository();
            Preconditions.checkState(repository != null);
        } catch (RuntimeException e) {
            throw new IOException(e);
        }

        GeoGitDataStore store = new GeoGitDataStore(geogit);
        if (defaultNamespace != null) {
            store.setNamespaceURI(defaultNamespace);
        }
        return store;
    }

}
