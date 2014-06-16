/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.geogit.cli.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.geogit.cli.CLICommand;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.StagingDatabase;

/**
 * Annotation for {@link CLICommand}s giving geogit a hint that the operation is read only for
 * {@link ObjectDatabase}, {@link StagingDatabase}, and any remote repository it may connect to.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {

}
