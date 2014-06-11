/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RevPersonTest {

    @Test
    public void testRevPersonConstructorAndAccessors() {
        RevPerson person = new RevPersonImpl("test name", "test.email@test.com", 12345, 54321);

        assertEquals("test name", person.getName().get());
        assertEquals("test.email@test.com", person.getEmail().get());
        assertEquals(12345, person.getTimestamp());
        assertEquals(54321, person.getTimeZoneOffset());
    }

    @Test
    public void testRevPersonToString() {
        RevPerson person = new RevPersonImpl("test name", "test.email@test.com", 12345, 54321);

        String nameAndEmail = person.toString();

        assertEquals("test name <test.email@test.com> 12345/54321", nameAndEmail);
    }

    @Test
    public void testRevPersonEquals() {
        RevPerson person = new RevPersonImpl("test name", "test.email@test.com", 12345, 54321);
        RevPerson person2 = new RevPersonImpl("kishmael", "kelsey.ishmael@lmnsolutions.com", 54321,
                12345);
        assertFalse(person.equals(person2));
        person2 = new RevPersonImpl("test name", "kelsey.ishmael@lmnsolutions.com", 54321, 12345);

        assertFalse(person.equals(person2));
        person2 = new RevPersonImpl("test name", "test.email@test.com", 54321, 12345);
        assertFalse(person.equals(person2));
        person2 = new RevPersonImpl("test name", "test.email@test.com", 12345, 12345);
        assertFalse(person.equals(person2));
        assertFalse(person.equals("blah"));
        assertTrue(person.equals(person));
    }
}
