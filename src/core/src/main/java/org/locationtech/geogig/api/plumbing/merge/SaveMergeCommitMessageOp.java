/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.plumbing.merge;

import java.io.File;
import java.net.URL;

import org.locationtech.geogig.api.AbstractGeoGitOp;
import org.locationtech.geogig.api.plumbing.ResolveGeogitDir;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

public class SaveMergeCommitMessageOp extends AbstractGeoGitOp<Void> {

    private String message;

    public SaveMergeCommitMessageOp setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    protected Void _call() {
        URL envHome = new ResolveGeogitDir(platform()).call().get();
        try {
            File file = new File(envHome.toURI());
            file = new File(file, "MERGE_MSG");
            Files.write(message, file, Charsets.UTF_8);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return null;
    }

}
