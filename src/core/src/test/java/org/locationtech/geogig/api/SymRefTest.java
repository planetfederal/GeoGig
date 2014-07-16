/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SymRefTest {

    @Test
    public void testSymRef() {
        Ref testRef = new Ref(Ref.REFS_PREFIX + "commit1", ObjectId.forString("Test Commit"));

        SymRef symRef = new SymRef("TestRef", testRef);

        assertEquals(testRef.getName(), symRef.getTarget());

        String symRefString = symRef.toString();

        assertEquals("SymRef[TestRef -> " + "Ref[" + testRef.getName() + " -> "
                + testRef.getObjectId().toString() + "]]", symRefString);
    }
}
