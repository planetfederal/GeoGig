/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.web.api;

import javax.annotation.Nullable;

/**
 * Provides an interface for a set of parameters keyed by a string value. Supports implementations
 * where a single key can be used multiple times.
 */
public interface ParameterSet {

    /**
     * Returns the value of the parameter with the given key, or {@code null} if the key wasn't
     * found. The first match will be returned.
     * 
     * @param key the key to search for
     * @return the value, or {@code null}
     */
    @Nullable
    public String getFirstValue(String key);

    /**
     * Returns the value of the parameter with the given key, or the default value if the key wasn't
     * found. The first match will be returned.
     * 
     * @param key the key to search for
     * @param defaultValue the value to return if the key is not found
     * @return the value, or {@code defaultValue} if the key was not found
     */
    public String getFirstValue(String key, String defaultValue);

    /**
     * Returns all values that match the specified key, or null if no values were found.
     * 
     * @param key the key to search for
     * @return an array of values, or {@code null}
     */
    @Nullable
    public String[] getValuesArray(String key);

}
