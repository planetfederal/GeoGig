/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.hooks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.locationtech.geogig.api.AbstractGeoGigOp;

/**
 * An annotation to indicate that a {@link AbstractGeoGigOp GeoGig operation} allows hooks
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Hookable {
    /**
     * the name used to identify the script files corresponding to the hooks for the class annotated
     * with this annotation
     */
    public String name();
}