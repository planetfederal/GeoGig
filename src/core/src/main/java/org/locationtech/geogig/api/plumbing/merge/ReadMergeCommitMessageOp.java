/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing.merge;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.locationtech.geogig.api.AbstractGeoGitOp;
import org.locationtech.geogig.api.plumbing.ResolveGeogitDir;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

public class ReadMergeCommitMessageOp extends AbstractGeoGitOp<String> {

    @Override
    protected String _call() {
        URL envHome = new ResolveGeogitDir(platform()).call().get();
        try {
            File file = new File(envHome.toURI());
            file = new File(file, "MERGE_MSG");
            if (!file.exists()) {
                return "";
            }
            List<String> lines = Files.readLines(file, Charsets.UTF_8);
            return Joiner.on("\n").join(lines);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}