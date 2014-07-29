/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.osm.internal;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;

@ThreadSafe
class QueueIterator<T> extends AbstractIterator<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueIterator.class);

    private BlockingQueue<T> queue;

    private int timeout;

    private TimeUnit timeoutUnit;

    private volatile boolean finish;

    public QueueIterator(int queueCapacity, int timeout, TimeUnit timeoutUnit) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        queue = new ArrayBlockingQueue<T>(queueCapacity);
    }

    public void finish() {
        this.finish = true;
    }

    public void put(T elem) {
        try {
            while (!finish && !queue.offer(elem, timeout, timeoutUnit)) {
                LOGGER.debug("queue.offer timed out after {} {}. retrying...", timeout, timeoutUnit);
            }
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected T computeNext() {
        try {
            T next = null;
            while (!finish && (next = queue.poll(timeout, timeoutUnit)) == null) {
                LOGGER.debug("queue.poll timed out after {} {}. retrying...", timeout, timeoutUnit);
            }
            if (next == null) {
                if (finish && !queue.isEmpty()) {
                    next = queue.take();
                } else {
                    return endOfData();
                }
            }
            return next;
        } catch (InterruptedException e) {
            return endOfData();
        }
    }

}
