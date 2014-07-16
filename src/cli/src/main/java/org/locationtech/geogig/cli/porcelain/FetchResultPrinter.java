/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.cli.porcelain;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import jline.console.ConsoleReader;

import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.porcelain.FetchResult;
import org.locationtech.geogig.api.porcelain.FetchResult.ChangedRef;
import org.locationtech.geogig.api.porcelain.FetchResult.ChangedRef.ChangeTypes;

class FetchResultPrinter {

    public static void print(FetchResult result, ConsoleReader console) throws IOException {
        for (Entry<String, List<ChangedRef>> entry : result.getChangedRefs().entrySet()) {
            console.println("From " + entry.getKey());

            for (ChangedRef ref : entry.getValue()) {
                String line;
                if (ref.getType() == ChangeTypes.CHANGED_REF) {
                    line = "   " + ref.getOldRef().getObjectId().toString().substring(0, 7) + ".."
                            + ref.getNewRef().getObjectId().toString().substring(0, 7) + "     "
                            + ref.getOldRef().localName() + " -> " + ref.getOldRef().getName();
                } else if (ref.getType() == ChangeTypes.ADDED_REF) {
                    String reftype = (ref.getNewRef().getName().startsWith(Ref.TAGS_PREFIX)) ? "tag"
                            : "branch";
                    line = " * [new " + reftype + "]     " + ref.getNewRef().localName() + " -> "
                            + ref.getNewRef().getName();
                } else if (ref.getType() == ChangeTypes.REMOVED_REF) {
                    line = " x [deleted]        (none) -> " + ref.getOldRef().getName();
                } else {
                    line = "   [deepened]       " + ref.getNewRef().localName();
                }
                console.println(line);
            }
        }
    }
}
