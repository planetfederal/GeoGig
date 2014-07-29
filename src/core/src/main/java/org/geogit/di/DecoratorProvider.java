/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.di;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class DecoratorProvider {

    private Set<Decorator> decorators;

    private Map<Class<?>, Object> singletonDecorators = Maps.newConcurrentMap();

    @Inject
    public DecoratorProvider(Set<Decorator> decorators) {
        this.decorators = decorators;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final T undecorated) {
        T decorated = undecorated;
        Class<? extends Object> undecoratedClass = decorated.getClass();
        {
            Object singletonDecorator = singletonDecorators.get(undecoratedClass);
            if (singletonDecorator != null) {
                return (T) singletonDecorator;
            }
        }

        for (Decorator decorator : decorators) {
            if (decorator.canDecorate(decorated)) {
                decorated = (T) decorator.decorate(decorated);
            }
        }
        if (isSingleton(undecoratedClass)) {
            singletonDecorators.put(undecoratedClass, decorated);
        }

        return decorated;
    }

    private boolean isSingleton(Class<? extends Object> c) {
        if (c.isAnnotationPresent(Singleton.class)) {
            return true;
        }
        Class<?> s = c.getSuperclass();
        if (s != null && isSingleton(s)) {
            return true;
        }
        Class<?>[] interfaces = c.getInterfaces();
        for (Class<?> i : interfaces) {
            return isSingleton(i);
        }
        return false;
    }

}
