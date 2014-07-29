/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.di;

public interface Decorator {

    public boolean canDecorate(Object instance);

    public <I> I decorate(I subject);
}
