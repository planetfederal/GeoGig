/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.geogit.api.hooks;

import java.io.File;

import javax.annotation.Nullable;

import org.geogit.api.AbstractGeoGitOp;

class ShellScriptHook implements CommandHook {

    private File preScript;

    private File postScript;

    public ShellScriptHook(@Nullable final File preScript, @Nullable final File postScript) {
        this.preScript = preScript;
        this.postScript = postScript;
    }

    @Override
    public <C extends AbstractGeoGitOp<?>> C pre(C command)
            throws CannotRunGeogitOperationException {

        if (preScript == null) {
            return command;
        }

        Scripting.runShellScript(preScript);

        return command;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T post(AbstractGeoGitOp<T> command, Object retVal, boolean success) throws Exception {

        if (postScript == null) {
            return (T) retVal;
        }

        if (success) {
            Scripting.runShellScript(preScript);
        }
        return (T) retVal;
    }

    @Override
    public boolean appliesTo(Class<? extends AbstractGeoGitOp<?>> clazz) {
        return true;
    }

}
