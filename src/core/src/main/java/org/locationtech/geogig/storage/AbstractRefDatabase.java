/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Throwables;

/**
 * Provides a base implementation for different representations of the {@link RefDatabase}.
 * 
 * @see RefDatabase
 */
public abstract class AbstractRefDatabase implements RefDatabase {

    Lock lock = new ReentrantLock();

    /**
     * Locks access to the main repository refs.
     * 
     * @throws TimeoutException
     */
    @Override
    public final void lock() throws TimeoutException {
        try {
            if (!lock.tryLock(30, TimeUnit.SECONDS)) {
                throw new TimeoutException("The attempt to lock the database timed out.");
            }
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    /**
     * Unlocks access to the main repository refs.
     */
    @Override
    public final void unlock() {
        lock.unlock();
    }

}
