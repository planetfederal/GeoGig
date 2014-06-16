/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.osm.cli.commands;

import static org.geogit.osm.internal.OSMUtils.NODE_TYPE_NAME;
import static org.geogit.osm.internal.OSMUtils.WAY_TYPE_NAME;
import static org.geogit.osm.internal.OSMUtils.nodeType;
import static org.geogit.osm.internal.OSMUtils.wayType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.management.relation.Relation;

import jline.console.ConsoleReader;

import org.geogit.api.DefaultProgressListener;
import org.geogit.api.FeatureBuilder;
import org.geogit.api.GeoGIT;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.ProgressListener;
import org.geogit.api.Ref;
import org.geogit.api.RevFeature;
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevFeatureTypeImpl;
import org.geogit.api.RevTree;
import org.geogit.api.SymRef;
import org.geogit.api.plumbing.FindTreeChild;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.ResolveGeogitDir;
import org.geogit.api.plumbing.ResolveTreeish;
import org.geogit.api.plumbing.RevObjectParse;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.cli.AbstractCommand;
import org.geogit.cli.CLICommand;
import org.geogit.cli.CommandFailedException;
import org.geogit.cli.GeogitCLI;
import org.geogit.osm.internal.history.Change;
import org.geogit.osm.internal.history.Changeset;
import org.geogit.osm.internal.history.HistoryDownloader;
import org.geogit.osm.internal.history.Node;
import org.geogit.osm.internal.history.Primitive;
import org.geogit.osm.internal.history.Way;
import org.geogit.repository.Repository;
import org.geogit.repository.StagingArea;
import org.geogit.repository.WorkingTree;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

//import org.geogit.api.Node;

/**
 *
 */
@Parameters(commandNames = "import-history", commandDescription = "Import OpenStreetmap history")
public class OSMHistoryImport extends AbstractCommand implements CLICommand {

    private static final GeometryFactory GEOMF = new GeometryFactory();

    @ParametersDelegate
    public HistoryImportArgs args = new HistoryImportArgs();

    @Override
    protected void runInternal(GeogitCLI cli) throws IOException {
        checkParameter(args.numThreads > 0 && args.numThreads < 7,
                "numthreads must be between 1 and 6");

        ConsoleReader console = cli.getConsole();

        final String osmAPIUrl = resolveAPIURL();

        final long startIndex;
        final long endIndex = args.endIndex;
        if (args.resume) {
            GeoGIT geogit = cli.getGeogit();
            long lastChangeset = getCurrentBranchChangeset(geogit);
            startIndex = 1 + lastChangeset;
        } else {
            startIndex = args.startIndex;
        }
        console.println("Obtaining OSM changesets " + startIndex + " to " + args.endIndex
                + " from " + osmAPIUrl);

        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("osm-history-fetch-thread-%d").build();
        final ExecutorService executor = Executors.newFixedThreadPool(args.numThreads,
                threadFactory);
        final File targetDir = resolveTargetDir();
        console.println("Downloading to " + targetDir.getAbsolutePath());
        console.println("Files will " + (args.keepFiles ? "" : " not ")
                + "be kept on the download directory.");
        console.flush();

        HistoryDownloader downloader;
        downloader = new HistoryDownloader(osmAPIUrl, targetDir, startIndex, endIndex, executor,
                args.keepFiles);
        try {
            importOsmHistory(cli, console, downloader);
        } finally {
            executor.shutdownNow();
            try {
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new CommandFailedException(e);
            }
        }
    }

    private File resolveTargetDir() throws IOException {
        final File targetDir;
        if (args.saveFolder == null) {
            try {
                File tmp = new File(System.getProperty("java.io.tmpdir"), "changesets.osm");
                tmp.mkdirs();
                targetDir = tmp;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        } else {
            if (!args.saveFolder.exists() && !args.saveFolder.mkdirs()) {
                throw new IllegalArgumentException("Unable to create directory "
                        + args.saveFolder.getAbsolutePath());
            }
            targetDir = args.saveFolder;
        }
        return targetDir;
    }

    private String resolveAPIURL() {
        String osmAPIUrl;
        if (args.useTestApiEndpoint) {
            osmAPIUrl = HistoryImportArgs.DEVELOPMENT_API_ENDPOINT;
        } else if (args.apiUrl.isEmpty()) {
            osmAPIUrl = HistoryImportArgs.DEFAULT_API_ENDPOINT;
        } else {
            osmAPIUrl = args.apiUrl.get(0);
        }
        return osmAPIUrl;
    }

    private void importOsmHistory(GeogitCLI cli, ConsoleReader console, HistoryDownloader downloader)
            throws IOException {
        Optional<Changeset> set;

        while ((set = downloader.fetchNextChangeset()).isPresent()) {
            Changeset changeset = set.get();

            String desc = "obtaining osm changeset " + changeset.getId() + "...";
            console.print(desc);
            console.flush();

            // ProgressListener listener = cli.getProgressListener();
            // listener.dispose();
            // listener.setCanceled(false);
            // listener.progress(0f);
            // listener.setDescription(desc);
            // listener.started();

            Iterator<Change> changes = changeset.getChanges().get();
            console.print("applying...");
            console.flush();
            insertAndAddChanges(cli, changes);
            // listener.progress(100f);
            // listener.complete();

            commit(cli, changeset);
        }
    }

    /**
     * @param cli
     * @param changeset
     * @throws IOException
     */
    private void commit(GeogitCLI cli, Changeset changeset) throws IOException {
        ConsoleReader console = cli.getConsole();
        console.print("Committing changeset " + changeset.getId() + "...");
        console.flush();

        GeoGIT geogit = cli.getGeogit();
        CommitOp command = geogit.command(CommitOp.class);
        command.setAllowEmpty(true);
        String message = "";
        if (changeset.getComment().isPresent()) {
            message = changeset.getComment().get() + "\nchangeset " + changeset.getId();
        } else {
            message = "changeset " + changeset.getId();
        }
        command.setMessage(message);
        final String userName = changeset.getUserName();
        command.setAuthor(userName, null);
        command.setAuthorTimestamp(changeset.getCreated());
        command.setAuthorTimeZoneOffset(0);// osm timestamps are in GMT

        if (userName != null) {
            command.setCommitter(userName, null);
        }
        command.setCommitterTimestamp(changeset.getClosed());
        command.setCommitterTimeZoneOffset(0);// osm timestamps are in GMT

        ProgressListener listener = cli.getProgressListener();
        listener.setProgress(0f);
        listener.started();
        command.setProgressListener(listener);
        try {
            command.call();
            updateBranchChangeset(geogit, changeset.getId());
            listener.complete();
            console.println("done.");
            console.flush();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * @param geogit
     * @param id
     * @throws IOException
     */
    private void updateBranchChangeset(GeoGIT geogit, long id) throws IOException {
        final File branchTrackingChangesetFile = getBranchTrackingFile(geogit);
        Preconditions.checkState(branchTrackingChangesetFile.exists());
        Files.write(String.valueOf(id), branchTrackingChangesetFile, Charset.forName("UTF-8"));
    }

    private long getCurrentBranchChangeset(GeoGIT geogit) throws IOException {
        final File branchTrackingChangesetFile = getBranchTrackingFile(geogit);
        Preconditions.checkState(branchTrackingChangesetFile.exists());
        String line = Files.readFirstLine(branchTrackingChangesetFile, Charset.forName("UTF-8"));
        if (line == null) {
            return 0;
        }
        long changeset = Long.parseLong(line);
        return changeset;
    }

    private File getBranchTrackingFile(GeoGIT geogit) throws IOException {
        final SymRef head = getHead(geogit);
        final String branch = head.getTarget();
        final URL geogitDirUrl = geogit.command(ResolveGeogitDir.class).call().get();
        File repoDir;
        try {
            repoDir = new File(geogitDirUrl.toURI());
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
        File branchTrackingFile = new File(new File(repoDir, "osm"), branch);
        Files.createParentDirs(branchTrackingFile);
        if (!branchTrackingFile.exists()) {
            Files.touch(branchTrackingFile);
        }
        return branchTrackingFile;
    }

    private SymRef getHead(GeoGIT geogit) {
        final Ref currentHead = geogit.command(RefParse.class).setName(Ref.HEAD).call().get();
        if (!(currentHead instanceof SymRef)) {
            throw new CommandFailedException("Cannot run on a dettached HEAD");
        }
        return (SymRef) currentHead;
    }

    /**
     * @param cli
     * @param changes
     * @throws IOException
     */
    private void insertAndAddChanges(GeogitCLI cli, final Iterator<Change> changes)
            throws IOException {
        if (!changes.hasNext()) {
            return;
        }
        final GeoGIT geogit = cli.getGeogit();
        final Repository repository = geogit.getRepository();
        final WorkingTree workTree = repository.workingTree();

        Map<Long, Coordinate> thisChangePointCache = new LinkedHashMap<Long, Coordinate>() {
            /** serialVersionUID */
            private static final long serialVersionUID = 1277795218777240552L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Coordinate> eldest) {
                return size() == 10000;
            }
        };

        int cnt = 0;

        Set<String> deletes = Sets.newHashSet();
        Multimap<String, SimpleFeature> insertsByParent = HashMultimap.create();

        while (changes.hasNext()) {
            Change change = changes.next();
            final String featurePath = featurePath(change);
            if (featurePath == null) {
                continue;// ignores relations
            }
            cnt++;
            final String parentPath = NodeRef.parentPath(featurePath);
            if (Change.Type.delete.equals(change.getType())) {
                deletes.add(featurePath);
            } else {
                final Primitive primitive = change.getNode().isPresent() ? change.getNode().get()
                        : change.getWay().get();
                final Geometry geom = parseGeometry(geogit, primitive, thisChangePointCache);
                if (geom instanceof Point) {
                    thisChangePointCache.put(Long.valueOf(primitive.getId()),
                            ((Point) geom).getCoordinate());
                }

                SimpleFeature feature = toFeature(primitive, geom);
                insertsByParent.put(parentPath, feature);
            }
        }

        for (String parentPath : insertsByParent.keySet()) {
            Collection<SimpleFeature> features = insertsByParent.get(parentPath);
            if (features.isEmpty()) {
                continue;
            }

            Iterator<? extends Feature> iterator = features.iterator();
            ProgressListener listener = new DefaultProgressListener();
            List<org.geogit.api.Node> insertedTarget = null;
            Integer collectionSize = Integer.valueOf(features.size());
            workTree.insert(parentPath, iterator, listener, insertedTarget, collectionSize);
        }
        if (!deletes.isEmpty()) {
            workTree.delete(deletes.iterator());
        }
        ConsoleReader console = cli.getConsole();
        console.print("Applied " + cnt + " changes, staging...");
        console.flush();

        geogit.command(AddOp.class).call();

        console.println("done.");
        console.flush();
    }

    /**
     * @param primitive
     * @param thisChangePointCache
     * @return
     */
    private Geometry parseGeometry(GeoGIT geogit, Primitive primitive,
            Map<Long, Coordinate> thisChangePointCache) {

        if (primitive instanceof Relation) {
            return null;
        }

        if (primitive instanceof Node) {
            Optional<Point> location = ((Node) primitive).getLocation();
            return location.orNull();
        }

        final Way way = (Way) primitive;
        final ImmutableList<Long> nodes = way.getNodes();

        StagingArea index = geogit.getRepository().index();

        FeatureBuilder featureBuilder = new FeatureBuilder(NODE_REV_TYPE);
        List<Coordinate> coordinates = Lists.newArrayList(nodes.size());
        FindTreeChild findTreeChild = geogit.command(FindTreeChild.class);
        findTreeChild.setIndex(true);
        ObjectId rootTreeId = geogit.command(ResolveTreeish.class).setTreeish(Ref.HEAD).call()
                .get();
        if (!rootTreeId.isNull()) {
            RevTree headTree = geogit.command(RevObjectParse.class).setObjectId(rootTreeId)
                    .call(RevTree.class).get();
            findTreeChild.setParent(headTree);
        }
        for (Long nodeId : nodes) {
            Coordinate coord = thisChangePointCache.get(nodeId);
            if (coord == null) {
                String fid = String.valueOf(nodeId);
                String path = NodeRef.appendChild(NODE_TYPE_NAME, fid);
                Optional<org.geogit.api.Node> ref = index.findStaged(path);
                if (!ref.isPresent()) {
                    Optional<NodeRef> nodeRef = findTreeChild.setChildPath(path).call();
                    if (nodeRef.isPresent()) {
                        ref = Optional.of(nodeRef.get().getNode());
                    } else {
                        ref = Optional.absent();
                    }
                }
                if (ref.isPresent()) {
                    org.geogit.api.Node nodeRef = ref.get();

                    RevFeature revFeature = index.getDatabase().getFeature(nodeRef.getObjectId());
                    String id = NodeRef.nodeFromPath(nodeRef.getName());
                    Feature feature = featureBuilder.build(id, revFeature);

                    Point p = (Point) ((SimpleFeature) feature).getAttribute("location");
                    if (p != null) {
                        coord = p.getCoordinate();
                        thisChangePointCache.put(Long.valueOf(nodeId), coord);
                    }
                }
            }
            if (coord != null) {
                coordinates.add(coord);
            }
        }
        if (coordinates.size() < 2) {
            return null;
        }
        return GEOMF.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
    }

    /**
     * @param change
     * @return
     */
    private String featurePath(Change change) {
        if (change.getRelation().isPresent()) {
            return null;// ignore relations for the time being
        }
        if (change.getNode().isPresent()) {
            String fid = String.valueOf(change.getNode().get().getId());
            return NodeRef.appendChild(NODE_TYPE_NAME, fid);
        }
        String fid = String.valueOf(change.getWay().get().getId());
        return NodeRef.appendChild(WAY_TYPE_NAME, fid);
    }

    private static final RevFeatureType NODE_REV_TYPE = RevFeatureTypeImpl.build(nodeType());

    private static SimpleFeature toFeature(Primitive feature, Geometry geom) {

        SimpleFeatureType ft = feature instanceof Node ? nodeType() : wayType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(ft);

        // "visible:Boolean,version:Int,timestamp:long,[location:Point | way:LineString];
        builder.set("visible", Boolean.valueOf(feature.isVisible()));
        builder.set("version", Integer.valueOf(feature.getVersion()));
        builder.set("timestamp", Long.valueOf(feature.getTimestamp()));
        builder.set("changeset", Long.valueOf(feature.getChangesetId()));

        String tags = buildTagsString(feature.getTags());
        builder.set("tags", tags);

        String user = feature.getUserName() + ":" + feature.getUserId();
        builder.set("user", user);

        if (feature instanceof Node) {
            builder.set("location", geom);
        } else if (feature instanceof Way) {
            builder.set("way", geom);
            String nodes = buildNodesString(((Way) feature).getNodes());
            builder.set("nodes", nodes);
        } else {
            throw new IllegalArgumentException();
        }

        String fid = String.valueOf(feature.getId());
        SimpleFeature simpleFeature = builder.buildFeature(fid);
        return simpleFeature;
    }

    /**
     * @param tags
     * @return
     */
    @Nullable
    private static String buildTagsString(Map<String, String> tags) {
        if (tags.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<Map.Entry<String, String>> it = tags.entrySet().iterator(); it.hasNext();) {
            Entry<String, String> e = it.next();
            String key = e.getKey();
            if (key == null || key.isEmpty()) {
                continue;
            }
            String value = e.getValue();
            sb.append(key).append(':').append(value);
            if (it.hasNext()) {
                sb.append(';');
            }
        }
        return sb.toString();
    }

    private static String buildNodesString(List<Long> nodeIds) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Long> it = nodeIds.iterator(); it.hasNext();) {
            Long node = it.next();
            sb.append(node);
            if (it.hasNext()) {
                sb.append(";");
            }
        }
        return sb.toString();

    }
}
