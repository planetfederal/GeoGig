/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.hooks;

import java.io.File;

import javax.annotation.Nullable;

import org.locationtech.geogig.api.AbstractGeoGigOp;

class ShellScriptHook implements CommandHook {

    private File preScript;

    private File postScript;

    public ShellScriptHook(@Nullable final File preScript, @Nullable final File postScript) {
        this.preScript = preScript;
        this.postScript = postScript;
    }

    @Override
    public <C extends AbstractGeoGigOp<?>> C pre(C command)
            throws CannotRunGeogigOperationException {

        if (preScript == null) {
            return command;
        }

        Scripting.runShellScript(preScript);

        return command;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T post(AbstractGeoGigOp<T> command, Object retVal, boolean success) throws Exception {

        if (postScript == null) {
            return (T) retVal;
        }

        if (success) {
            Scripting.runShellScript(preScript);
        }
        return (T) retVal;
    }

    @Override
    public boolean appliesTo(Class<? extends AbstractGeoGigOp<?>> clazz) {
        return true;
    }

}
