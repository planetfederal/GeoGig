/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.cli;

import com.beust.jcommander.JCommander;

/**
 * Interface for cli command extensions, allowing to provide their own configured {@link JCommander}
 * , and hence support command line extensions (a'la git-svn, for example
 * {@code geogit osm <command> <args>...}, {@code geogit pg <command> <args>...}).
 */
public interface CLICommandExtension {

    /**
     * @return the JCommander parser for this extension
     * @see JCommander
     */
    public JCommander getCommandParser();
}
