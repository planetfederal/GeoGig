/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api;

import com.google.common.base.Optional;

/**
 * The GeoGig identity of a single individual, composed of a name and email address.
 */
public interface RevPerson {

    /**
     * @return the name
     */
    public abstract Optional<String> getName();

    /**
     * @return the email
     */
    public abstract Optional<String> getEmail();

    /**
     * @return this person's timestamp, as milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public abstract long getTimestamp();

    /**
     * @return the time zone offset from UTC, in milliseconds
     */
    public abstract int getTimeZoneOffset();

}