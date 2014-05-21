/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.osm.internal.history;

import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Envelope;

/**
 *
 */
public class Changeset {

    private long id;

    private String userName;

    private long userId;

    private long created;

    private long closed;

    private boolean open;

    private Envelope wgs84Bounds;

    private String comment;

    private Map<String, String> tags;

    private Supplier<Optional<Iterator<Change>>> changes;

    public Changeset() {
        tags = Maps.newHashMap();
        userId = -1;
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public long getUserId() {
        return userId;
    }

    public long getCreated() {
        return created;
    }

    public long getClosed() {
        return closed;
    }

    public boolean isOpen() {
        return open;
    }

    public Optional<Envelope> getWgs84Bounds() {
        return Optional.fromNullable(wgs84Bounds);
    }

    public Optional<String> getComment() {
        return Optional.fromNullable(comment);
    }

    public Supplier<Optional<Iterator<Change>>> getChanges() {
        if (changes == null) {
            return Suppliers.ofInstance(Optional.<Iterator<Change>> absent());
        }
        return changes;
    }

    void setChanges(Supplier<Optional<Iterator<Change>>> changes) {
        this.changes = changes;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    void setId(long id) {
        this.id = id;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    void setUserId(long userId) {
        this.userId = userId;
    }

    void setCreated(long created) {
        this.created = created;
    }

    void setClosed(long closed) {
        this.closed = closed;
    }

    void setOpen(boolean open) {
        this.open = open;
    }

    void setWgs84Bounds(Envelope wgs84Bounds) {
        this.wgs84Bounds = wgs84Bounds;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

}
