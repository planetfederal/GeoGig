/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.cli;

import org.locationtech.geogig.cli.annotation.RequiresRepository;
import org.locationtech.geogig.cli.porcelain.Config;
import org.locationtech.geogig.cli.porcelain.Help;
import org.locationtech.geogig.cli.porcelain.Init;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Base interface for command executed through the command line interface.
 * <p>
 * Command classes that require a live geogit repository to exist in order to be run are encouraged
 * to be marked with the {@link RequiresRepository @RequiresRepository} annotation, to be sure
 * {@link #run(GeogitCLI)} is only going to be called with a valid repository in place.
 * <p>
 * Commands that don't necessarily require a repository to run (e.g. {@link Init init}, {@link Help
 * help}, {@link Config config}, etc} shall not be annotated with {@link RequiresRepository
 * @RequiresRepository}, although they're free to check {@link GeogitCLI#getGeogit()} for nullity if
 * they need to perform one or another task depending on the precense or not of a repository.
 * 
 */
public interface CLICommand {

    /**
     * Executes the CLI command represented by the implementation.
     * <p>
     * When this method is called, the command line arguments are known to have been correctly
     * parsed by {@link JCommander}, which would have thrown a {@link ParameterException} if the
     * arguments couldn't be parsed before this method had a chance to run. That said,
     * implementations of this method are free to perform any additional argument validation, and
     * are required to throw an {@link InvalidParameterException} if the argument validation fails.
     * 
     * @param cli the cli instance representing the context where the command is run, and giving it
     *        access to it (console, platform, and repository).
     * @throws InvalidParameterException if any of the command line arguments is invalid or missing
     * @throws CommandFailedException if the CLI command succeeded in calling the internal
     *         operation, which then failed for a <b>recoverable</b> reason.
     * @throws RuntimeException for any other unknown cause of failure to execute the operation,
     *         generally propagated back from it.
     */
    void run(GeogitCLI cli) throws InvalidParameterException, CommandFailedException;

}
