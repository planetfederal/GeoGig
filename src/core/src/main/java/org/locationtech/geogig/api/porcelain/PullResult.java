/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api.porcelain;

import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.porcelain.MergeOp.MergeReport;

import com.google.common.base.Optional;

public class PullResult {

    private Ref oldRef = null;

    private Ref newRef = null;

    private String remoteName = null;

    private FetchResult fetchResult = null;

    private Optional<MergeReport> mergeReport = Optional.absent();

    public String getRemoteName() {
        return remoteName;
    }

    void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public FetchResult getFetchResult() {
        return fetchResult;
    }

    void setFetchResult(FetchResult fetchResult) {
        this.fetchResult = fetchResult;
    }

    public Ref getOldRef() {
        return oldRef;
    }

    void setOldRef(Ref oldRef) {
        this.oldRef = oldRef;
    }

    public Ref getNewRef() {
        return newRef;
    }

    void setNewRef(Ref newRef) {
        this.newRef = newRef;
    }

    public Optional<MergeReport> getMergeReport() {
        return mergeReport;
    }

    void setMergeReport(Optional<MergeReport> mergeReport) {
        this.mergeReport = mergeReport;
    }
}
