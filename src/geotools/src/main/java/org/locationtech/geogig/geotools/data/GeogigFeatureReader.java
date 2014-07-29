/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.geotools.data;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterators.filter;
import static com.google.common.collect.Iterators.transform;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.geotools.data.FeatureReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.spatial.ReprojectingFilterVisitor;
import org.geotools.filter.visitor.SpatialFilterVisitor;
import org.geotools.util.logging.Logging;
import org.locationtech.geogig.api.Bounded;
import org.locationtech.geogig.api.Bucket;
import org.locationtech.geogig.api.Context;
import org.locationtech.geogig.api.FeatureBuilder;
import org.locationtech.geogig.api.Node;
import org.locationtech.geogig.api.NodeRef;
import org.locationtech.geogig.api.Ref;
import org.locationtech.geogig.api.RevFeature;
import org.locationtech.geogig.api.RevObject;
import org.locationtech.geogig.api.RevTree;
import org.locationtech.geogig.api.plumbing.FindTreeChild;
import org.locationtech.geogig.api.plumbing.LsTreeOp;
import org.locationtech.geogig.api.plumbing.LsTreeOp.Strategy;
import org.locationtech.geogig.api.plumbing.RevObjectParse;
import org.locationtech.geogig.storage.NodePathStorageOrder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;

/**
 *
 */
class GeogigFeatureReader<T extends FeatureType, F extends Feature> implements FeatureReader<T, F>,
        Iterator<F> {

    private static final Logger LOGGER = Logging.getLogger(GeogigFeatureReader.class);

    private SimpleFeatureType schema;

    private Stats stats;

    private Iterator<SimpleFeature> features;

    @Nullable
    private Integer offset;

    @Nullable
    private Integer maxFeatures;

    private static class Stats implements Predicate<Bounded> {
        public int featureHits, featureMisses, treeHits, treeMisses, bucketHits, bucketMisses;

        private Envelope bounds;

        public Stats(Envelope bounds) {
            this.bounds = bounds;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Hits/misses:\n");
            sb.append("Trees: ").append(treeHits).append('/').append(treeMisses).append('\n');
            sb.append("Features: ").append(featureHits).append('/').append(featureMisses)
                    .append('\n');
            sb.append("Buckets: ").append(bucketHits).append('/').append(bucketMisses).append('\n');
            return sb.toString();
        }

        @Override
        public boolean apply(Bounded bounded) {
            boolean intersects = bounds.isNull() ? true : bounded.intersects(bounds);
            if (bounded instanceof Bucket) {
                // {
                // Envelope e = new Envelope();
                // bounded.expand(e);
                // stats.geoms.add(JTS.toGeometry(e));
                // }
                if (intersects)
                    bucketHits++;
                else
                    bucketMisses++;
            } else {
                Node node;
                if (bounded instanceof NodeRef) {
                    node = ((NodeRef) bounded).getNode();
                } else {
                    node = (Node) bounded;
                }
                if (node.getType().equals(RevObject.TYPE.TREE)) {
                    if (intersects)
                        treeHits++;
                    else
                        treeMisses++;
                } else {
                    if (intersects)
                        featureHits++;
                    else
                        featureMisses++;
                }
            }

            return true;
        }
    }

    /**
     * @param commandLocator
     * @param schema
     * @param maxFeatures
     * @param offset
     * @param typeTree
     * @param filter
     * @param queryBounds
     */
    public GeogigFeatureReader(final Context commandLocator, final SimpleFeatureType schema,
            final Filter origFilter, final String typeTreePath, @Nullable final String headRef,
            @Nullable Integer offset, @Nullable Integer maxFeatures) {

        this.schema = schema;
        this.offset = offset;
        this.maxFeatures = maxFeatures;

        final String effectiveHead = headRef == null ? Ref.WORK_HEAD : headRef;
        final String typeTreeRefSpec = effectiveHead + ":" + typeTreePath;
        final Optional<RevTree> parentTree = commandLocator.command(RevObjectParse.class)
                .setRefSpec(typeTreeRefSpec).call(RevTree.class);

        Preconditions.checkArgument(parentTree.isPresent(), "Feature type tree not found: %s",
                typeTreeRefSpec);

        final Filter filter = reprojectFilter(origFilter);
        final Envelope queryBounds = getQueryBounds(filter);

        Predicate<Bounded> refBoundsFilter = alwaysTrue();
        if (!queryBounds.isNull()) {
            refBoundsFilter = new Predicate<Bounded>() {
                private final Envelope env = queryBounds;

                @Override
                public boolean apply(final Bounded bounded) {
                    boolean intersects = bounded.intersects(env);
                    return intersects;
                }
            };

            this.stats = new Stats(queryBounds);
            refBoundsFilter = and(stats, refBoundsFilter);
        }

        Iterator<NodeRef> featureRefs;

        if (filter instanceof Id) {
            final Function<FeatureId, NodeRef> idToRef;
            idToRef = new FindFeatureRefFunction(commandLocator, parentTree.get());
            Iterator<FeatureId> featureIds = getSortedFidsInNaturalOrder((Id) filter);
            featureRefs = filter(transform(featureIds, idToRef), notNull());
        } else {
            featureRefs = commandLocator.command(LsTreeOp.class)
                    .setStrategy(Strategy.FEATURES_ONLY).setReference(typeTreeRefSpec)
                    .setBoundsFilter(refBoundsFilter).call();
        }

        final boolean filterSupportedByRefs = Filter.INCLUDE.equals(filter)
                || filter instanceof BBOX;

        if (filterSupportedByRefs) {
            featureRefs = applyRefsOffsetLimit(featureRefs);
        }

        NodeRefToFeature refToFeature = new NodeRefToFeature(commandLocator, schema);
        final Iterator<SimpleFeature> featuresUnfiltered = transform(featureRefs, refToFeature);

        FilterPredicate filterPredicate = new FilterPredicate(filter);
        Iterator<SimpleFeature> featuresFiltered = filter(featuresUnfiltered, filterPredicate);
        if (!filterSupportedByRefs) {
            featuresFiltered = applyFeaturesOffsetLimit(featuresFiltered);
        }
        this.features = featuresFiltered;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getFeatureType() {
        return (T) schema;
    }

    @Override
    public void close() throws IOException {
        if (stats != null) {
            LOGGER.info("geogig reader stats: " + stats.toString());
        }
    }

    @Override
    public boolean hasNext() {
        return features.hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public F next() {
        return (F) features.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private Iterator<SimpleFeature> applyFeaturesOffsetLimit(Iterator<SimpleFeature> features) {
        if (offset != null) {
            Iterators.advance(features, offset.intValue());
        }
        if (maxFeatures != null) {
            features = Iterators.limit(features, maxFeatures.intValue());
        }
        return features;
    }

    private Iterator<NodeRef> applyRefsOffsetLimit(Iterator<NodeRef> featureRefs) {
        if (offset != null) {
            Iterators.advance(featureRefs, offset.intValue());
        }
        if (maxFeatures != null) {
            featureRefs = Iterators.limit(featureRefs, maxFeatures.intValue());
        }
        return featureRefs;
    }

    private Iterator<FeatureId> getSortedFidsInNaturalOrder(Id filter) {

        final Set<Identifier> identifiers = filter.getIdentifiers();

        Iterator<FeatureId> featureIds = filter(filter(identifiers.iterator(), FeatureId.class),
                notNull());

        // used for the returned featrures to be in "natural" order
        final Comparator<String> requestOrderMatchingStorageOrder = new NodePathStorageOrder();
        Comparator<FeatureId> requestOrder = new Comparator<FeatureId>() {
            @Override
            public int compare(FeatureId o1, FeatureId o2) {
                return requestOrderMatchingStorageOrder.compare(o1.getID(), o2.getID());
            }
        };
        TreeSet<FeatureId> sortedFids = Sets.newTreeSet(requestOrder);
        sortedFids.addAll(ImmutableList.copyOf(featureIds));
        return sortedFids.iterator();
    }

    private static class FindFeatureRefFunction implements Function<FeatureId, NodeRef> {

        private FindTreeChild command;

        private RevTree parentTree;

        public FindFeatureRefFunction(Context commandLocator, RevTree featureTypeTree) {
            this.parentTree = featureTypeTree;
            this.command = commandLocator.command(FindTreeChild.class);
        }

        @Override
        @Nullable
        public NodeRef apply(final FeatureId fid) {
            final String featureName = fid.getID();

            Optional<NodeRef> featureRef = command.setParent(parentTree).setChildPath(featureName)
                    .setIndex(true).call();
            return featureRef.orNull();
        }
    };

    private static class NodeRefToFeature implements Function<NodeRef, SimpleFeature> {

        private RevObjectParse parseRevFeatureCommand;

        private FeatureBuilder featureBuilder;

        public NodeRefToFeature(Context commandLocator, SimpleFeatureType schema) {
            this.featureBuilder = new FeatureBuilder(schema);
            this.parseRevFeatureCommand = commandLocator.command(RevObjectParse.class);
        }

        @Override
        public SimpleFeature apply(final NodeRef featureRef) {
            Optional<RevFeature> revFeature = parseRevFeatureCommand.setObjectId(
                    featureRef.objectId()).call(RevFeature.class);
            Preconditions.checkState(revFeature.isPresent());

            String id = featureRef.name();
            Feature feature = featureBuilder.build(id, revFeature.get());
            return (SimpleFeature) feature;
        }
    };

    private static final class FilterPredicate implements Predicate<SimpleFeature> {
        private Filter filter;

        public FilterPredicate(final Filter filter) {
            this.filter = filter;
        }

        @Override
        public boolean apply(SimpleFeature feature) {
            return filter.evaluate(feature);
        }
    }

    private Envelope getQueryBounds(Filter filter) {

        final Envelope queryBounds = new Envelope();
        Envelope bounds = (Envelope) filter.accept(new ExtractBounds(), queryBounds);
        if (bounds != null) {
            queryBounds.expandToInclude(bounds);
        }
        return queryBounds;
    }

    /**
     * @param filter
     * @return
     */
    private Filter reprojectFilter(Filter filter) {
        if (hasSpatialFilter(filter)) {
            CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
            if (crs == null) {
                LOGGER.fine("Not reprojecting filter to native CRS because feature type does not declare a CRS");

            } else {

                FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2();

                filter = (Filter) filter.accept(new ReprojectingFilterVisitor(factory, schema),

                null);

            }
        }
        return filter;
    }

    private boolean hasSpatialFilter(Filter filter) {
        SpatialFilterVisitor spatialFilterVisitor = new SpatialFilterVisitor();
        filter.accept(spatialFilterVisitor, null);
        return spatialFilterVisitor.hasSpatialFilter();
    }
}
