/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.bdbje;

import static com.sleepycat.je.OperationStatus.NOTFOUND;
import static com.sleepycat.je.OperationStatus.SUCCESS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nullable;

import org.geogit.api.ObjectId;
import org.geogit.repository.Hints;
import org.geogit.repository.RepositoryConnectionException;
import org.geogit.storage.ConfigDatabase;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.SynchronizedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.CacheMode;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

/**
 * Implementation of {@link GraphDatabase} backed by a BerkeleyDB Java Edition database.
 * <p>
 * Implementation note: Since this is the only kind of mutable state we maintain, this
 * implementation extends {@link SynchronizedGraphDatabase} to avoid concurrent threads stepping
 * over each other's feet and overriding graph relations. An alternate solution would be to
 * serialize writes and have free threaded reads.
 * </p>
 */
public class JEGraphDatabase extends SynchronizedGraphDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JEGraphDatabase.class);

    static final String ENVIRONMENT_NAME = "graph";

    @Inject
    public JEGraphDatabase(final ConfigDatabase config, final EnvironmentBuilder envProvider,
            final Hints hints) {
        super(new Impl(config, envProvider, hints));
    }

    private static class Impl implements GraphDatabase {

        private static final GraphNodeBinding BINDING = new GraphNodeBinding();

        private EnvironmentBuilder envProvider;

        /**
         * Lazily loaded, do not access it directly but through {@link #createEnvironment()}
         */
        protected Environment env;

        protected Database graphDb;

        private final String envName;

        private final ConfigDatabase configDb;

        private final String databaseName = "GraphDatabase";

        private final boolean readOnly;

        public Impl(final ConfigDatabase config, final EnvironmentBuilder envProvider,
                final Hints hints) {
            this.configDb = config;
            this.envProvider = envProvider;
            this.envName = JEGraphDatabase.ENVIRONMENT_NAME;
            this.readOnly = hints.getBoolean(Hints.OBJECTS_READ_ONLY);
        }

        @Override
        public void open() {
            if (isOpen()) {
                LOGGER.trace("Environment {} already open", env.getHome());
                return;
            }
            this.graphDb = createDatabase();
            // System.err.println("---> " + getClass().getName() + ".open() " + env.getHome());

            LOGGER.debug("Graph database opened at {}. Transactional: {}", env.getHome(), graphDb
                    .getConfig().getTransactional());
        }

        protected Database createDatabase() {

            Environment environment;
            try {
                environment = createEnvironment(readOnly);
            } catch (EnvironmentLockedException e) {
                throw new IllegalStateException(
                        "The repository is already open by another process for writing", e);
            }

            if (!environment.getDatabaseNames().contains(databaseName)) {
                if (readOnly) {
                    environment.close();
                    try {
                        environment = createEnvironment(false);
                    } catch (EnvironmentLockedException e) {
                        throw new IllegalStateException(String.format(
                                "Environment open readonly but database %s does not exist.",
                                databaseName));
                    }
                }
                DatabaseConfig dbConfig = new DatabaseConfig();
                dbConfig.setAllowCreate(true);
                Database openDatabase = environment.openDatabase(null, databaseName, dbConfig);
                openDatabase.close();
                environment.flushLog(true);
                environment.close();
                environment = createEnvironment(readOnly);
            }

            Database database;
            try {
                LOGGER.debug("Opening GraphDatabase at {}", environment.getHome());

                DatabaseConfig dbConfig = new DatabaseConfig();
                dbConfig.setCacheMode(CacheMode.MAKE_COLD);
                dbConfig.setKeyPrefixing(false);// can result in a slightly smaller db size

                dbConfig.setReadOnly(readOnly);
                boolean transactional = environment.getConfig().getTransactional();
                dbConfig.setTransactional(transactional);
                dbConfig.setDeferredWrite(!transactional);

                database = environment.openDatabase(null, databaseName, dbConfig);
            } catch (RuntimeException e) {
                if (environment != null) {
                    environment.close();
                }
                throw e;
            }
            this.env = environment;
            return database;

        }

        /**
         * @return creates and returns the environment
         */
        private synchronized Environment createEnvironment(boolean readOnly)
                throws com.sleepycat.je.EnvironmentLockedException {
            Environment env = envProvider.setRelativePath(this.envName).setReadOnly(readOnly).get();

            return env;
        }

        @Override
        public void configure() throws RepositoryConnectionException {
            RepositoryConnectionException.StorageType.GRAPH.configure(configDb, "bdbje", "0.1");
        }

        @Override
        public void checkConfig() throws RepositoryConnectionException {
            RepositoryConnectionException.StorageType.GRAPH.verify(configDb, "bdbje", "0.1");
        }

        @Override
        public boolean isOpen() {
            return graphDb != null;
        }

        @Override
        public void close() {
            if (env == null) {
                LOGGER.trace("Database already closed.");
                return;
            }
            // System.err.println("<--- " + getClass().getName() + ".close() " + env.getHome());
            final File envHome = env.getHome();
            try {
                LOGGER.debug("Closing graph database at {}", envHome);
                if (graphDb != null) {
                    graphDb.close();
                    graphDb = null;
                }
                LOGGER.trace("GraphDatabase closed. Closing environment...");
                if (!readOnly) {
                    env.sync();
                    env.cleanLog();
                }
            } finally {
                env.close();
                env = null;
            }
            LOGGER.debug("Database {} closed.", envHome);
        }

        @Override
        protected void finalize() {
            if (isOpen()) {
                LOGGER.warn("JEGraphDatabase %s was not closed. Forcing close at finalize()",
                        env.getHome());
                close();
            }
        }

        private static class NodeData {
            public ObjectId id;

            public List<ObjectId> outgoing;

            public List<ObjectId> incoming;

            public Map<String, String> properties;

            @Nullable
            public ObjectId mappedTo;

            public NodeData(ObjectId id, List<ObjectId> parents) {
                this(id, ObjectId.NULL, new ArrayList<ObjectId>(parents),
                        new ArrayList<ObjectId>(2), new HashMap<String, String>());
            }

            NodeData(ObjectId id, ObjectId mappedTo, List<ObjectId> parents,
                    List<ObjectId> children, Map<String, String> properties) {
                this.id = id;
                this.mappedTo = mappedTo;
                this.outgoing = parents;
                this.incoming = children;
                this.properties = properties;
            }

            public NodeData(ObjectId id) {
                this(id, ImmutableList.<ObjectId> of());
            }

            public boolean isSparse() {
                return properties.containsKey(SPARSE_FLAG) ? Boolean.valueOf(properties
                        .get(SPARSE_FLAG)) : false;
            }
        }

        protected NodeData getNodeInternal(final ObjectId id, final boolean failIfNotFound) {
            Preconditions.checkNotNull(id, "id");
            DatabaseEntry key = new DatabaseEntry(id.getRawValue());
            DatabaseEntry data = new DatabaseEntry();

            final LockMode lockMode = LockMode.READ_UNCOMMITTED;
            Transaction transaction = null;
            OperationStatus operationStatus = graphDb.get(transaction, key, data, lockMode);
            if (NOTFOUND.equals(operationStatus)) {
                if (failIfNotFound) {
                    throw new IllegalArgumentException("Graph Object does not exist: "
                            + id.toString() + " at " + env.getHome().getAbsolutePath());
                }
                return null;
            }
            NodeData node = BINDING.entryToObject(data);
            return node;
        }

        private boolean putNodeInternal(final Transaction transaction, final ObjectId id,
                final NodeData node) throws IOException {

            DatabaseEntry key = new DatabaseEntry(id.getRawValue());
            DatabaseEntry data = new DatabaseEntry();
            BINDING.objectToEntry(node, data);

            final OperationStatus status = graphDb.put(transaction, key, data);

            return SUCCESS.equals(status);
        }

        private void abort(@Nullable Transaction transaction) {
            if (transaction != null) {
                try {
                    transaction.abort();
                } catch (Exception e) {
                    LOGGER.error("Error aborting transaction", e);
                }
            }
        }

        private void commit(@Nullable Transaction transaction) {
            if (transaction != null) {
                try {
                    transaction.commit();
                } catch (Exception e) {
                    LOGGER.error("Error committing transaction", e);
                }
            }
        }

        @Nullable
        private Transaction newTransaction() {
            final boolean transactional = graphDb.getConfig().getTransactional();
            if (transactional) {
                TransactionConfig txConfig = new TransactionConfig();
                txConfig.setReadUncommitted(true);
                Optional<String> durability = configDb.get("bdbje.object_durability");
                if ("safe".equals(durability.orNull())) {
                    txConfig.setDurability(Durability.COMMIT_SYNC);
                } else {
                    txConfig.setDurability(Durability.COMMIT_WRITE_NO_SYNC);
                }
                Transaction transaction = env.beginTransaction(null, txConfig);
                return transaction;
            }
            return null;
        }

        @Override
        public boolean exists(ObjectId commitId) {
            Preconditions.checkNotNull(commitId, "id");

            DatabaseEntry key = new DatabaseEntry(commitId.getRawValue());
            DatabaseEntry data = new DatabaseEntry();
            // tell db not to retrieve data
            data.setPartial(0, 0, true);

            final LockMode lockMode = LockMode.READ_UNCOMMITTED;
            Transaction transaction = null;
            OperationStatus status = graphDb.get(transaction, key, data, lockMode);
            return SUCCESS == status;
        }

        @Override
        public ImmutableList<ObjectId> getParents(ObjectId commitId)
                throws IllegalArgumentException {
            Builder<ObjectId> listBuilder = new ImmutableList.Builder<ObjectId>();
            NodeData node = getNodeInternal(commitId, false);
            if (node != null) {
                return listBuilder.addAll(node.outgoing).build();
            }
            return listBuilder.build();
        }

        @Override
        public ImmutableList<ObjectId> getChildren(ObjectId commitId)
                throws IllegalArgumentException {
            Builder<ObjectId> listBuilder = new ImmutableList.Builder<ObjectId>();
            NodeData node = getNodeInternal(commitId, false);
            if (node != null) {
                return listBuilder.addAll(node.incoming).build();
            }
            return listBuilder.build();
        }

        @Override
        public boolean put(ObjectId commitId, ImmutableList<ObjectId> parentIds) {
            NodeData node = getNodeInternal(commitId, false);
            boolean updated = false;
            final Transaction transaction = newTransaction();
            try {
                if (node == null) {
                    node = new NodeData(commitId, parentIds);
                    updated = true;
                }
                for (ObjectId parent : parentIds) {
                    if (!node.outgoing.contains(parent)) {
                        node.outgoing.add(parent);
                        updated = true;
                    }
                    NodeData parentNode = getNodeInternal(parent, false);
                    if (parentNode == null) {
                        parentNode = new NodeData(parent);
                        updated = true;
                    }
                    if (!parentNode.incoming.contains(commitId)) {
                        parentNode.incoming.add(commitId);
                        updated = true;
                    }
                    putNodeInternal(transaction, parent, parentNode);
                }
                putNodeInternal(transaction, commitId, node);
                commit(transaction);
            } catch (Exception e) {
                abort(transaction);
                throw Throwables.propagate(e);
            }
            return updated;
        }

        @Override
        public void map(ObjectId mapped, ObjectId original) {
            NodeData node = getNodeInternal(mapped, false);
            if (node == null) {
                // didn't exist
                node = new NodeData(mapped);
            }
            node.mappedTo = original;
            final Transaction transaction = newTransaction();
            try {
                putNodeInternal(transaction, mapped, node);
                commit(transaction);
            } catch (Exception e) {
                abort(transaction);
                throw Throwables.propagate(e);
            }
        }

        @Override
        public ObjectId getMapping(ObjectId commitId) {
            NodeData node = getNodeInternal(commitId, true);
            return node.mappedTo;
        }

        @Override
        public int getDepth(ObjectId commitId) {
            int depth = 0;

            Queue<ObjectId> q = Lists.newLinkedList();
            NodeData node = getNodeInternal(commitId, true);
            Iterables.addAll(q, node.outgoing);

            List<ObjectId> next = Lists.newArrayList();
            while (!q.isEmpty()) {
                depth++;
                while (!q.isEmpty()) {
                    ObjectId n = q.poll();
                    NodeData parentNode = getNodeInternal(n, true);
                    List<ObjectId> parents = Lists.newArrayList(parentNode.outgoing);
                    if (parents.size() == 0) {
                        return depth;
                    }

                    Iterables.addAll(next, parents);
                }

                q.addAll(next);
                next.clear();
            }

            return depth;
        }

        @Override
        public void setProperty(ObjectId commitId, String propertyName, String propertyValue) {
            NodeData node = getNodeInternal(commitId, true);
            node.properties.put(propertyName, propertyValue);
            final Transaction transaction = newTransaction();
            try {
                putNodeInternal(transaction, commitId, node);
                commit(transaction);
            } catch (Exception e) {
                abort(transaction);
                throw Throwables.propagate(e);
            }
        }

        private class JEGraphNode extends GraphNode {
            NodeData node;

            List<GraphEdge> edges;

            public JEGraphNode(NodeData node) {
                this.node = node;
                this.edges = null;
            }

            @Override
            public ObjectId getIdentifier() {
                return node.id;
            }

            @Override
            public Iterator<GraphEdge> getEdges(final Direction direction) {
                if (edges == null) {
                    edges = new LinkedList<GraphEdge>();
                    Iterator<ObjectId> nodeEdges = node.incoming.iterator();
                    while (nodeEdges.hasNext()) {
                        ObjectId otherNode = nodeEdges.next();
                        edges.add(new GraphEdge(new JEGraphNode(getNodeInternal(otherNode, true)),
                                this));
                    }

                    nodeEdges = node.outgoing.iterator();
                    while (nodeEdges.hasNext()) {
                        ObjectId otherNode = nodeEdges.next();
                        edges.add(new GraphEdge(this, new JEGraphNode(getNodeInternal(otherNode,
                                true))));
                    }
                }

                final GraphNode myNode = this;

                return Iterators.filter(edges.iterator(), new Predicate<GraphEdge>() {
                    @Override
                    public boolean apply(GraphEdge input) {
                        switch (direction) {
                        case OUT:
                            return input.getFromNode() == myNode;
                        case IN:
                            return input.getToNode() == myNode;
                        default:
                            break;
                        }
                        return true;
                    }
                });
            }

            @Override
            public boolean isSparse() {
                return node.isSparse();
            }

        }

        @Override
        public GraphNode getNode(ObjectId id) {
            return new JEGraphNode(getNodeInternal(id, true));
        }

        @Override
        public void truncate() {
            // TODO Auto-generated method stub

        }

        private static class GraphNodeBinding extends TupleBinding<NodeData> {

            private static final ObjectIdBinding OID = new ObjectIdBinding();

            private static final OidListBinding OIDLIST = new OidListBinding();

            private static final PropertiesBinding PROPS = new PropertiesBinding();

            @Override
            public NodeData entryToObject(TupleInput input) {
                ObjectId id = OID.entryToObject(input);
                ObjectId mappedTo = OID.entryToObject(input);
                List<ObjectId> outgoing = OIDLIST.entryToObject(input);
                List<ObjectId> incoming = OIDLIST.entryToObject(input);
                Map<String, String> properties = PROPS.entryToObject(input);

                NodeData nodeData = new NodeData(id, mappedTo, outgoing, incoming, properties);
                return nodeData;
            }

            @Override
            public void objectToEntry(NodeData node, TupleOutput output) {
                OID.objectToEntry(node.id, output);
                OID.objectToEntry(node.mappedTo, output);
                OIDLIST.objectToEntry(node.outgoing, output);
                OIDLIST.objectToEntry(node.incoming, output);
                PROPS.objectToEntry(node.properties, output);
            }

            private static class ObjectIdBinding extends TupleBinding<ObjectId> {

                @Nullable
                @Override
                public ObjectId entryToObject(TupleInput input) {
                    int size = input.read();
                    if (size == 0) {
                        return ObjectId.NULL;
                    }
                    Preconditions.checkState(ObjectId.NUM_BYTES == size);
                    byte[] hash = new byte[size];
                    Preconditions.checkState(size == input.read(hash));
                    return ObjectId.createNoClone(hash);
                }

                @Override
                public void objectToEntry(@Nullable ObjectId object, TupleOutput output) {
                    if (null == object || object.isNull()) {
                        output.write(0);
                    } else {
                        output.write(ObjectId.NUM_BYTES);
                        output.write(object.getRawValue());
                    }
                }
            }

            private static class OidListBinding extends TupleBinding<List<ObjectId>> {
                private static final ObjectIdBinding OID = new ObjectIdBinding();

                @Override
                public List<ObjectId> entryToObject(TupleInput input) {
                    int len = input.readInt();
                    List<ObjectId> list = new ArrayList<ObjectId>((int) (1.5 * len));
                    for (int i = 0; i < len; i++) {
                        list.add(OID.entryToObject(input));
                    }
                    return list;
                }

                @Override
                public void objectToEntry(List<ObjectId> list, TupleOutput output) {
                    int len = list.size();
                    output.writeInt(len);
                    for (int i = 0; i < len; i++) {
                        OID.objectToEntry(list.get(i), output);
                    }
                }

            }

            private static class PropertiesBinding extends TupleBinding<Map<String, String>> {

                @Override
                public Map<String, String> entryToObject(TupleInput input) {
                    int len = input.readInt();
                    Map<String, String> props = new HashMap<String, String>();
                    for (int i = 0; i < len; i++) {
                        String k = input.readString();
                        String v = input.readString();
                        props.put(k, v);
                    }
                    return props;
                }

                @Override
                public void objectToEntry(Map<String, String> props, TupleOutput output) {
                    output.writeInt(props.size());
                    for (Map.Entry<String, String> e : props.entrySet()) {
                        output.writeString(e.getKey());
                        output.writeString(e.getValue());
                    }
                }
            }
        }
    }
}
