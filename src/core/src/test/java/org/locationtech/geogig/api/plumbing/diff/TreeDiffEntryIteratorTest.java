/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.api.plumbing.diff;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.geogig.api.Bucket;
import org.locationtech.geogig.api.Node;
import org.locationtech.geogig.api.NodeRef;
import org.locationtech.geogig.api.ObjectId;
import org.locationtech.geogig.api.RevObject.TYPE;
import org.locationtech.geogig.api.RevTree;
import org.locationtech.geogig.api.RevTreeImpl;
import org.locationtech.geogig.storage.NodeStorageOrder;
import org.locationtech.geogig.storage.ObjectDatabase;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 *
 */
public class TreeDiffEntryIteratorTest extends Assert {

    private ObjectDatabase mockDb;

    @Before
    public void setUp() throws Exception {
        mockDb = mock(ObjectDatabase.class);
    }

    @Test
    public void testChildrenChildrenEmpty() {

        RevTree leftTree = childenTree();
        RevTree rightTree = childenTree();

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of();

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenChildrenLeftEmpty() {

        RevTree leftTree = childenTree();
        RevTree rightTree = childenTree("0", "11", "2", "bb");

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);

        ImmutableSet<DiffEntry> expected = ImmutableSet.of(entry(null, null, "0", "11"),
                entry(null, null, "2", "bb"));

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenChildrenRightEmpty() {

        RevTree leftTree = childenTree("0", "11", "2", "bb");
        RevTree rightTree = childenTree();

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);

        ImmutableSet<DiffEntry> expected = ImmutableSet.of(entry("0", "11", null, null),
                entry("2", "bb", null, null));

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenChildrenSameTree() {

        RevTree leftTree = childenTree("0", "11", "2", "bb");
        RevTree rightTree = childenTree("0", "11", "2", "bb");

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);

        assertEquals(ImmutableSet.of(), diffset);
    }

    @Test
    public void testChildrenChildrenModification() {

        RevTree leftTree = childenTree("1", "aa", "2", "bb");
        RevTree rightTree = childenTree("1", "aa", "2", "ee");

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of(entry("2", "bb", "2", "ee"));
        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenChildren() {

        RevTree leftTree = childenTree("1", "aa", "2", "bb", "3", "cc");
        RevTree rightTree = childenTree("0", "0a", "2", "bb", "3", "c0", "4", "dd");

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of(//
                entry("1", "aa", null, null),//
                entry(null, null, "0", "0a"),//
                entry("3", "cc", "3", "c0"),//
                entry(null, null, "4", "dd")//
                );

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenChildrenWithNewSubtree() {

        RevTree childTree1 = childenTree("p1", "aa", "p2", "bb");

        RevTree leftTree = childenTree("1", "aa");
        RevTree rightTree = childenTree("1", "aa", "tree1", childTree1.getId().toString());

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of(//
                entry(null, null, "tree1/p1", "aa"),//
                entry(null, null, "tree1/p2", "bb")//
                );

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenChildrenWithRemovedSubtree() {

        RevTree childTree1 = childenTree("p1", "aa", "p2", "bb");

        RevTree leftTree = childenTree("1", "aa", "tree1", childTree1.getId().toString());
        RevTree rightTree = childenTree("1", "aa");

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of(//
                entry("tree1/p1", "aa", null, null),//
                entry("tree1/p2", "bb", null, null)//
                );

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenChildrenWithChangedSubtree() {

        RevTree childTreeV1 = childenTree("p1", "aa", "p2", "bb");
        RevTree childTreeV2 = childenTree("p1", "a1", "p2", "b2");

        RevTree leftTree = childenTree("1", "aa", "tree1", childTreeV1.getId().toString());
        RevTree rightTree = childenTree("1", "aa", "tree1", childTreeV2.getId().toString());

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of(//
                entry("tree1/p1", "aa", "tree1/p1", "a1"),//
                entry("tree1/p2", "bb", "tree1/p2", "b2")//
                );

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenChildrenWithChangedNestedSubtree() {

        RevTree subtree1_1 = childenTree("t11", "01", "t12", "bb");
        RevTree subtree1_2 = childenTree("t11", "02", "t12", "bc", "t13", "cc");

        RevTree subtree1 = childenTree("tree3", subtree1_1.getId().toString());
        RevTree subtree2 = childenTree("tree3", subtree1_2.getId().toString());

        RevTree childTreeV1 = childenTree("tree2", subtree1.getId().toString());
        RevTree childTreeV2 = childenTree("tree2", subtree2.getId().toString());

        RevTree leftTree = childenTree("1", "aa", "tree1", childTreeV1.getId().toString());
        RevTree rightTree = childenTree("1", "ab", "tree1", childTreeV2.getId().toString());

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of(//
                entry("1", "aa", "1", "ab"),//
                entry("tree1/tree2/tree3/t11", "01", "tree1/tree2/tree3/t11", "02"),//
                entry("tree1/tree2/tree3/t12", "bb", "tree1/tree2/tree3/t12", "bc"),//
                entry(null, null, "tree1/tree2/tree3/t13", "cc")//
                );

        assertEquals(expected, diffset);
    }

    @Test
    public void testBucketEmptyChildrenEmpty() {

        RevTree leftTree = bucketTree();
        RevTree rightTree = childenTree();

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of();

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenEmptyBucketEmpty() {

        RevTree leftTree = childenTree();
        RevTree rightTree = bucketTree();

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of();

        assertEquals(expected, diffset);
    }

    @Test
    public void testChildrenBucket() {

        RevTree leftTree = childenTree();
        RevTree rightTree = bucketTree();

        ImmutableSet<DiffEntry> diffset = diffSet(leftTree, rightTree);
        ImmutableSet<DiffEntry> expected = ImmutableSet.of();

        assertEquals(expected, diffset);
    }

    private RevTree bucketTree() {

        ObjectId id = ObjectId.forString("null");
        Map<Integer, Bucket> bucketTrees = ImmutableMap.of();
        RevTreeImpl tree = RevTreeImpl.createNodeTree(id, 0, 0, bucketTrees);

        when(mockDb.getTree(eq(id))).thenReturn(tree);

        return tree;
    }

    private RevTree childenTree(@Nullable String... pathIdKvps) {

        TreeSet<Node> features = Sets.newTreeSet(new NodeStorageOrder());
        TreeSet<Node> trees = Sets.newTreeSet(new NodeStorageOrder());

        if (pathIdKvps != null) {
            for (int i = 0; i < pathIdKvps.length; i += 2) {
                String path = pathIdKvps[i];
                String idStr = pathIdKvps[i + 1];
                NodeRef ref = ref(path, idStr);
                if (ref.getType().equals(TYPE.FEATURE)) {
                    features.add(ref.getNode());
                } else {
                    trees.add(ref.getNode());
                }
            }
        }

        ObjectId id = pathIdKvps == null ? ObjectId.forString("null") : ObjectId.forString(Joiner
                .on(" ").join(pathIdKvps));

        ImmutableList<Node> sortedRefs = ImmutableList.copyOf(features);
        long size = sortedRefs.size();

        ImmutableList<Node> treeRefs = ImmutableList.copyOf(trees);
        RevTreeImpl tree = RevTreeImpl.createLeafTree(id, size, sortedRefs, treeRefs);

        when(mockDb.getTree(eq(id))).thenReturn(tree);

        return tree;
    }

    private DiffEntry entry(@Nullable String oldPath, @Nullable String oldId,
            @Nullable String newPath, @Nullable String newId) {

        NodeRef left = oldPath == null ? null : ref(oldPath, oldId);
        NodeRef right = newPath == null ? null : ref(newPath, newId);

        return new DiffEntry(left, right);
    }

    private NodeRef ref(String fullPath, String idStr) {
        idStr += Strings.repeat("0", ObjectId.NULL.toString().length() - idStr.length());
        ObjectId objectId = ObjectId.valueOf(idStr);

        String path = NodeRef.nodeFromPath(fullPath);
        String parentPath = NodeRef.parentPath(fullPath);
        TYPE type = path.startsWith("tree") ? TYPE.TREE : TYPE.FEATURE;
        NodeRef ref = new NodeRef(Node.create(path, objectId, ObjectId.NULL, type, null),
                parentPath, ObjectId.NULL);
        return ref;
    }

    private ImmutableSet<DiffEntry> diffSet(RevTree leftTree, RevTree rightTree) {
        NodeRef leftNodeRef = new NodeRef(Node.create("", leftTree.getId(), ObjectId.NULL,
                TYPE.TREE, null), "", ObjectId.NULL);
        NodeRef rightNodeRef = new NodeRef(Node.create("", leftTree.getId(), ObjectId.NULL,
                TYPE.TREE, null), "", ObjectId.NULL);
        boolean reportTrees = false;
        ImmutableSet<DiffEntry> diffset = ImmutableSet.copyOf(new TreeDiffEntryIterator(
                leftNodeRef, rightNodeRef, leftTree, rightTree, reportTrees, true, mockDb));
        return diffset;
    }
}
