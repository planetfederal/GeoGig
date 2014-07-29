/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.hooks;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.AbstractGeoGitOp.CommandListener;
import org.geogit.di.Decorator;

/**
 * An interceptor for the call() method in GeoGit operations that allow hooks
 * 
 */
public class CommandHooksDecorator implements Decorator {

    @SuppressWarnings("unchecked")
    @Override
    public boolean canDecorate(Object instance) {
        boolean canDecorate = instance instanceof AbstractGeoGitOp;
        if (canDecorate) {
            canDecorate &= instance.getClass().isAnnotationPresent(Hookable.class);
            canDecorate |= Hookables
                    .hasClasspathHooks((Class<? extends AbstractGeoGitOp<?>>) instance.getClass());
        }
        return canDecorate;
    }

    @Override
    public <I> I decorate(I subject) {
        AbstractGeoGitOp<?> op = (AbstractGeoGitOp<?>) subject;
        CommandHookChain callChain = CommandHookChain.builder().command(op).build();
        if (!callChain.isEmpty()) {
            op.addListener(new HooksListener(callChain));
        }
        return subject;
    }

    private static class HooksListener implements CommandListener {

        private CommandHookChain callChain;

        public HooksListener(CommandHookChain callChain) {
            this.callChain = callChain;
        }

        @Override
        public void preCall(AbstractGeoGitOp<?> command) {
            callChain.runPreHooks();
        }

        @Override
        public void postCall(AbstractGeoGitOp<?> command, Object result, boolean success) {
            callChain.runPostHooks(result, success);
        }

    }
}
