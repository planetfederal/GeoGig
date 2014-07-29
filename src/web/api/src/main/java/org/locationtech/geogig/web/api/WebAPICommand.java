/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.web.api;

/**
 * A basic interface from which all Web API commands will be derived.
 */
public interface WebAPICommand {

    /**
     * Runs the command with the given {@link CommandContext}.
     * 
     * @param context the context for this command
     */
    void run(CommandContext context);

}
