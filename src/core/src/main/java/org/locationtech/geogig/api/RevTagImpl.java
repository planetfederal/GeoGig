/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An annotated tag.
 * 
 */
public class RevTagImpl extends AbstractRevObject implements RevTag {

    private String name;

    private ObjectId commit;

    private String message;

    private RevPerson tagger;

    /**
     * Constructs a new {@code RevTag} with the given {@link ObjectId}, name, commit id and message.
     * 
     * @param id the {@code ObjectId} to use for this tag
     * @param name the name of the tag
     * @param commitId the {@code ObjectId} of the commit that this tag points to
     * @param message the tag message
     */
    public RevTagImpl(final ObjectId id, final String name, final ObjectId commitId,
            final String message, RevPerson tagger) {
        super(id);
        checkNotNull(name);
        checkNotNull(commitId);
        checkNotNull(message);
        checkNotNull(tagger);
        this.name = name;
        this.commit = commitId;
        this.message = message;
        this.tagger = tagger;
    }

    @Override
    public TYPE getType() {
        return TYPE.TAG;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return the message
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @return the tagger
     */
    @Override
    public RevPerson getTagger() {
        return tagger;
    }

    /**
     * @return the {@code ObjectId} of the commit that this tag points to
     */
    @Override
    public ObjectId getCommitId() {
        return commit;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RevTag) && super.equals(o)) {
            return false;
        }
        RevTag t = (RevTag) o;
        return equal(getName(), t.getName()) && equal(getCommitId(), t.getCommitId())
                && equal(getMessage(), t.getMessage());
    }
}
