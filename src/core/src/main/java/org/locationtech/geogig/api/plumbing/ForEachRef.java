/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing;

import org.locationtech.geogig.api.AbstractGeoGitOp;
import org.locationtech.geogig.api.Ref;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

/**
 * Update the object name stored in a {@link Ref} safely.
 * <p>
 * 
 */
public class ForEachRef extends AbstractGeoGitOp<ImmutableSet<Ref>> {

    private Predicate<Ref> filter;

    public ForEachRef setFilter(Predicate<Ref> filter) {
        this.filter = filter;
        return this;
    }

    public ForEachRef setPrefixFilter(final String prefix) {
        this.filter = new Predicate<Ref>() {
            @Override
            public boolean apply(Ref ref) {
                return ref != null && ref.getName().startsWith(prefix);
            }
        };
        return this;
    }

    /**
     * @return the new value of the ref
     */
    @Override
    protected  ImmutableSet<Ref> _call() {

        @SuppressWarnings("unchecked")
        final Predicate<Ref> filter = (Predicate<Ref>) (this.filter == null ? Predicates
                .alwaysTrue() : this.filter);

        ImmutableSet.Builder<Ref> refs = new ImmutableSet.Builder<Ref>();
        for (String refName : refDatabase().getAll().keySet()) {
            Optional<Ref> ref = command(RefParse.class).setName(refName).call();
            if (ref.isPresent() && filter.apply(ref.get())) {
                Ref accepted = ref.get();
                refs.add(accepted);
            }
        }
        return refs.build();
    }
}
