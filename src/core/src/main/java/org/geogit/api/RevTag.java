/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

public interface RevTag extends RevObject {

    /**
     * @return the name
     */
    public abstract String getName();

    /**
     * @return the message
     */
    public abstract String getMessage();

    /**
     * @return the tagger
     */
    public abstract RevPerson getTagger();

    /**
     * @return the {@code ObjectId} of the commit that this tag points to
     */
    public abstract ObjectId getCommitId();

}