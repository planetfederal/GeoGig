/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.web.api.commands;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.geogit.api.Context;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.api.RevPerson;
import org.geogit.api.plumbing.LsTreeOp;
import org.geogit.api.plumbing.ParseTimestamp;
import org.geogit.api.plumbing.RevParse;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.porcelain.DiffOp;
import org.geogit.api.porcelain.LogOp;
import org.geogit.web.api.AbstractWebAPICommand;
import org.geogit.web.api.CommandContext;
import org.geogit.web.api.CommandResponse;
import org.geogit.web.api.ResponseWriter;
import org.geotools.util.Range;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * List certain statistics of repository.
 */

public class StatisticsWebOp extends AbstractWebAPICommand {

    String path;

    String since;

    String until;

    public void setPath(String path) {
        this.path = path;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    /**
     * Runs the command and builds the appropriate response
     * 
     * @param context - the context to use for this command
     */
    @Override
    public void run(CommandContext context) {
        final Context geogit = this.getCommandLocator(context);
        final List<FeatureTypeStats> stats = Lists.newArrayList();
        LogOp logOp = geogit.command(LogOp.class).setFirstParentOnly(true);
        final Iterator<RevCommit> log;
        if (since != null && !since.trim().isEmpty()) {
            Date untilTime = new Date();
            Date sinceTime = new Date(geogit.command(ParseTimestamp.class).setString(since).call());
            logOp.setTimeRange(new Range<Date>(Date.class, sinceTime, untilTime));
        }
        if (this.until != null) {
            Optional<ObjectId> until;
            until = geogit.command(RevParse.class).setRefSpec(this.until).call();
            Preconditions.checkArgument(until.isPresent(), "Object not found '%s'", this.until);
            logOp.setUntil(until.get());
        }

        LsTreeOp lsTreeOp = geogit.command(LsTreeOp.class)
                .setStrategy(LsTreeOp.Strategy.TREES_ONLY);
        if (path != null && !path.trim().isEmpty()) {
            lsTreeOp.setReference(path);
            logOp.addPath(path);
        }
        final Iterator<NodeRef> treeIter = lsTreeOp.call();

        while (treeIter.hasNext()) {
            NodeRef node = treeIter.next();
            stats.add(new FeatureTypeStats(node.path(), context.getGeoGIT().getRepository()
                    .getTree(node.objectId()).size()));
        }
        log = logOp.call();

        RevCommit firstCommit = null;
        RevCommit lastCommit = null;
        int totalCommits = 0;
        final List<RevPerson> authors = Lists.newArrayList();

        if (log.hasNext()) {
            lastCommit = log.next();
            authors.add(lastCommit.getAuthor());
            totalCommits++;
        }
        while (log.hasNext()) {
            firstCommit = log.next();
            RevPerson newAuthor = firstCommit.getAuthor();
            // If the author isn't defined, use the committer for the purposes of statistics.
            if (!newAuthor.getName().isPresent() && !newAuthor.getEmail().isPresent()) {
                newAuthor = firstCommit.getCommitter();
            }
            if (newAuthor.getName().isPresent() || newAuthor.getEmail().isPresent()) {
                boolean authorFound = false;
                for (RevPerson author : authors) {
                    if (newAuthor.getName().equals(author.getName())
                            && newAuthor.getEmail().equals(author.getEmail())) {
                        authorFound = true;
                        break;
                    }
                }
                if (!authorFound) {
                    authors.add(newAuthor);
                }
            }
            totalCommits++;
        }
        int addedFeatures = 0;
        int modifiedFeatures = 0;
        int removedFeatures = 0;
        if (since != null && !since.trim().isEmpty() && firstCommit != null && lastCommit != null) {
            final Iterator<DiffEntry> diff = geogit.command(DiffOp.class)
                    .setOldVersion(firstCommit.getId()).setNewVersion(lastCommit.getId())
                    .setFilter(path).call();
            while (diff.hasNext()) {
                DiffEntry entry = diff.next();
                if (entry.changeType() == DiffEntry.ChangeType.ADDED) {
                    addedFeatures++;
                } else if (entry.changeType() == DiffEntry.ChangeType.MODIFIED) {
                    modifiedFeatures++;
                } else {
                    removedFeatures++;
                }
            }
        }

        final RevCommit first = firstCommit;
        final RevCommit last = lastCommit;
        final int total = totalCommits;
        final int added = addedFeatures;
        final int modified = modifiedFeatures;
        final int removed = removedFeatures;
        context.setResponseContent(new CommandResponse() {
            @Override
            public void write(ResponseWriter out) throws Exception {
                out.start(true);
                out.writeStatistics(stats, first, last, total, authors, added, modified, removed);
                out.finish();
            }
        });
    }

    public class FeatureTypeStats {
        long numFeatures;

        String featureTypeName;

        public FeatureTypeStats(String name, long numFeatures) {
            this.numFeatures = numFeatures;
            this.featureTypeName = name;
        }

        public long getNumFeatures() {
            return numFeatures;
        }

        public String getName() {
            return featureTypeName;
        }
    };
}
