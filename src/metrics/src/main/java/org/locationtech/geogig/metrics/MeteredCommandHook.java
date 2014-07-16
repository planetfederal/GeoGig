/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.locationtech.geogig.metrics;

import static org.locationtech.geogig.metrics.MetricsModule.COMMAND_STACK_LOGGER;
import static org.locationtech.geogig.metrics.MetricsModule.METRICS_ENABLED;
import static org.locationtech.geogig.metrics.MetricsModule.METRICS_LOGGER;

import java.util.concurrent.TimeUnit;

import org.locationtech.geogig.api.AbstractGeoGitOp;
import org.locationtech.geogig.api.Platform;
import org.locationtech.geogig.api.hooks.CannotRunGeogitOperationException;
import org.locationtech.geogig.api.hooks.CommandHook;
import org.locationtech.geogig.api.porcelain.ConfigException;
import org.locationtech.geogig.api.porcelain.ConfigException.StatusCode;
import org.locationtech.geogig.storage.ConfigDatabase;

public class MeteredCommandHook implements CommandHook {

    private static final double toMillisFactor = 1.0 / TimeUnit.MILLISECONDS.toNanos(1L);

    /**
     * @return {@code true}, applies to all ops
     */
    @Override
    public boolean appliesTo(Class<? extends AbstractGeoGitOp<?>> clazz) {
        return true;
    }

    @Override
    public <C extends AbstractGeoGitOp<?>> C pre(C command)
            throws CannotRunGeogitOperationException {
        Boolean enabled;
        if (command.context().repository() == null) {
            return command;
        }
        ConfigDatabase configDb = command.context().configDatabase();
        try {
            enabled = configDb.get(METRICS_ENABLED, Boolean.class).or(Boolean.FALSE);
        } catch (ConfigException e) {
            if (StatusCode.INVALID_LOCATION.equals(e.statusCode)) {
                enabled = Boolean.FALSE;
            } else {
                throw e;
            }
        }
        if (!enabled.booleanValue()) {
            return command;
        }

        final Platform platform = command.context().platform();
        final long startTime = platform.currentTimeMillis();
        final long nanoTime = platform.nanoTime();
        final String name = command.getClass().getSimpleName();
        CallStack stack = CallStack.push(name, startTime, nanoTime);
        command.getClientData().put("metrics.callStack", stack);
        return command;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T post(AbstractGeoGitOp<T> command, Object retVal, boolean success) throws Exception {

        CallStack stack = (CallStack) command.getClientData().get("metrics.callStack");
        if (stack == null || command.context().repository() == null) {
            return (T) retVal;
        }

        final Platform platform = command.context().platform();
        long endTime = platform.nanoTime();
        stack = CallStack.pop(endTime, success);
        long ellapsed = stack.getEllapsedNanos();

        double millis = endTime * toMillisFactor;
        METRICS_LOGGER.info("{}, {}, {}, {}", stack.getName(), stack.getStartTimeMillis(), millis,
                success);
        if (stack.isRoot()) {
            COMMAND_STACK_LOGGER.info("{}", stack.toString(TimeUnit.MILLISECONDS));
        }

        return (T) retVal;
    }

}
