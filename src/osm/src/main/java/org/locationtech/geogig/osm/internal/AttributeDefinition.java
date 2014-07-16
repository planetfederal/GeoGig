/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.osm.internal;

import org.locationtech.geogig.storage.FieldType;

import com.google.gson.annotations.Expose;

public class AttributeDefinition {

    @Expose
    private String name;

    @Expose
    private FieldType type;

    public AttributeDefinition(String name, FieldType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public FieldType getType() {
        return type;
    }

    public boolean equals(Object o) {
        if (o instanceof AttributeDefinition) {
            AttributeDefinition at = (AttributeDefinition) o;
            return name.equals(at.name) && at.type.equals(type);
        } else {
            return false;
        }
    }

}
