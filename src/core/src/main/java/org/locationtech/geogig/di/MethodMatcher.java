/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.di;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.common.base.Throwables;
import com.google.inject.matcher.AbstractMatcher;

public class MethodMatcher extends AbstractMatcher<Method> {

    private final Method target;

    public MethodMatcher(Method target) {
        this.target = target;
    }

    public MethodMatcher(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            this.target = clazz.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean matches(final Method t) {

        return !t.isSynthetic() && target.getName().equals(t.getName())
                && Arrays.equals(target.getParameterTypes(), t.getParameterTypes());
    }

}
