/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.remote;

import java.io.IOException;
import java.io.OutputStream;

import org.locationtech.geogig.api.RevObject;
import org.locationtech.geogig.storage.ObjectSerializingFactory;

import com.google.common.base.Supplier;

public class ObjectFunnels {

    public static ObjectFunnel newFunnel(OutputStream out, ObjectSerializingFactory serializer) {
        return new DirectFunnel(out, serializer);
    }

    public static ObjectFunnel newFunnel(Supplier<OutputStream> outputFactory,
            ObjectSerializingFactory serializer, final int byteLimit) {
        return new SizeLimitingFunnel(outputFactory, serializer, byteLimit);
    }

    private static class DirectFunnel implements ObjectFunnel {

        private OutputStream out;

        private ObjectSerializingFactory serializer;

        public DirectFunnel(OutputStream out, ObjectSerializingFactory serializer) {
            this.out = out;
            this.serializer = serializer;
        }

        @Override
        public void funnel(RevObject object) throws IOException {
            out.write(object.getId().getRawValue());
            serializer.createObjectWriter(object.getType()).write(object, out);
        }

        @Override
        public void close() throws IOException {
            OutputStream out = this.out;
            this.out = null;
            if (out != null) {
                out.close();
            }
        }
    }

    private static class SizeLimitingFunnel implements ObjectFunnel {

        private Supplier<OutputStream> outputFactory;

        private final ObjectSerializingFactory serializer;

        private final int byteLimit;

        private OutputStream currentTarget;

        public SizeLimitingFunnel(Supplier<OutputStream> outputFactory,
                ObjectSerializingFactory serializer, int byteLimit) {
            this.outputFactory = outputFactory;
            this.serializer = serializer;
            this.byteLimit = byteLimit;
        }

        @Override
        public void funnel(RevObject object) throws IOException {
            OutputStream out = getCurrentTarget();
            out.write(object.getId().getRawValue());
            serializer.createObjectWriter(object.getType()).write(object, out);
            out.flush();
        }

        private OutputStream getCurrentTarget() throws IOException {
            if (currentTarget == null) {
                currentTarget = outputFactory.get();
            } /*
               * else if (currentTarget.getCount() >= byteLimit) { currentTarget.close();
               * currentTarget = new CountingOutputStream(outputFactory.get()); }
               */

            return currentTarget;
        }

        @Override
        public void close() throws IOException {
            System.err.println("Closing SizeLimitingFunnel");
            System.err.flush();
            OutputStream currentTarget = this.currentTarget;
            this.currentTarget = null;
            if (currentTarget != null) {
                currentTarget.close();
            }
            outputFactory = null;
        }
    }
}
