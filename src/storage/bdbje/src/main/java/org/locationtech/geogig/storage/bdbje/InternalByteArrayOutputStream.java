/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.storage.bdbje;

import java.io.ByteArrayOutputStream;

final class InternalByteArrayOutputStream extends ByteArrayOutputStream {

    public InternalByteArrayOutputStream(int initialBuffSize) {
        super(initialBuffSize);
    }

    public byte[] bytes() {
        return super.buf;
    }

    public int size() {
        return super.count;
    }
}