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
import com.google.common.io.CountingOutputStream;

public class ObjectFunnels {

    public static ObjectFunnel newFunnel(OutputStream out, ObjectSerializingFactory serializer) {
        return new DirectFunnel(out, serializer);
    }

    public static ObjectFunnel newFunnel(final Supplier<OutputStream> outputFactory,
            final ObjectSerializingFactory serializer, final int byteSoftLimit) {

        return new SizeLimitingFunnel(outputFactory, serializer, byteSoftLimit);
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

        private final int byteSofLimit;

        private CountingOutputStream currentTarget;

        public SizeLimitingFunnel(Supplier<OutputStream> outputFactory,
                ObjectSerializingFactory serializer, final int byteSoftLimit) {
            this.outputFactory = outputFactory;
            this.serializer = serializer;
            this.byteSofLimit = byteSoftLimit;
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
                currentTarget = new CountingOutputStream(outputFactory.get());
            } else if (currentTarget.getCount() >= byteSofLimit) {
                System.err.printf("Closing stream and opening a new one, reached %,d bytes.\n",
                        currentTarget.getCount());
                currentTarget.close();
                currentTarget = new CountingOutputStream(outputFactory.get());
            }

            return currentTarget;
        }

        @Override
        public void close() throws IOException {
            OutputStream currentTarget = this.currentTarget;
            this.currentTarget = null;
            if (currentTarget != null) {
                currentTarget.close();
            }
            outputFactory = null;
        }

    }
}
