/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.hooks;

import java.util.ServiceLoader;

import javax.annotation.concurrent.ThreadSafe;

import org.locationtech.geogig.api.AbstractGeoGitOp;

/**
 * An interface that defines hooks for {@link AbstractGeoGitOp command} written in Java as opposed
 * to a scripting language.
 * <p>
 * Implementations of this interface are discovered using the standard Java {@link ServiceLoader}
 * SPI lookup, by looking for implementing class names at
 * {@code META-INF/services/org.geogit.api.hooks.CommandHook} resources.
 * <p>
 * Implementations must have a default constructor (or no explicit constructor at all), and must be
 * thread safe.
 */
@ThreadSafe
public interface CommandHook {

    public <C extends AbstractGeoGitOp<?>> C pre(C command)
            throws CannotRunGeogitOperationException;

    public <T> T post(AbstractGeoGitOp<T> command, Object retVal, boolean success) throws Exception;

    public boolean appliesTo(Class<? extends AbstractGeoGitOp<?>> clazz);
}
