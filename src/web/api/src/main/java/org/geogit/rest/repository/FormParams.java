/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.rest.repository;

import org.geogit.web.api.ParameterSet;
import org.restlet.data.Form;

/**
 *
 */
class FormParams implements ParameterSet {

    private Form options;

    /**
     * @param options
     */
    public FormParams(Form options) {
        this.options = options;
    }

    @Override
    public String getFirstValue(String key) {
        return options.getFirstValue(key);
    }

    @Override
    public String[] getValuesArray(String key) {
        String values = options.getValues(key);
        if (values == null) {
            return new String[0];
        }
        String[] split = values.split(",");
        return split;
    }

    @Override
    public String getFirstValue(String key, String defaultValue) {
        return options.getFirstValue(key, defaultValue);
    }

}
