/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

/**
 * Wraps a ResultSet consisting of a single string column in an iterable.
 * 
 * @author Justin Deoliveira, Boundless
 * 
 */
public class StringResultSetIterable implements Iterable<String> {

    ResultSet rs;

    Connection cx;

    StringResultSetIterable(ResultSet rs, Connection cx) {
        this.rs = rs;
        this.cx = cx;
    }

    @Override
    public Iterator<String> iterator() {
        return new AbstractIterator<String>() {
            @Override
            protected String computeNext() {
                try {
                    if (!rs.next()) {
                        rs.close();
                        cx.close();
                        return endOfData();
                    }

                    return rs.getString(1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
