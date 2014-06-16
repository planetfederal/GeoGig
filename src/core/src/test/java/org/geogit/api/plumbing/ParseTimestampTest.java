/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.geogit.api.Context;
import org.geogit.api.GeoGIT;
import org.geogit.api.MemoryModule;
import org.geogit.api.Platform;
import org.geogit.api.TestPlatform;
import org.geogit.di.GeogitModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.util.Modules;

/**
 *
 */
public class ParseTimestampTest extends Assert {

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static final Date REFERENCE_DATE;// = new Date(1972, 10, 10, 10, 10);
    static {
        try {
            REFERENCE_DATE = format.parse("1972-10-10 10:10:10");
        } catch (ParseException e) {
            throw Throwables.propagate(e);
        }
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ParseTimestamp command;

    private GeoGIT fakeGeogit;

    @Before
    public void setUp() {

        File workingDirectory = tempFolder.newFolder("mockWorkingDir");
        Platform testPlatform = new TestPlatform(workingDirectory) {
            @Override
            public long currentTimeMillis() {
                return REFERENCE_DATE.getTime();
            }
        };
        Context injector = Guice.createInjector(
                Modules.override(new GeogitModule()).with(new MemoryModule(testPlatform)))
                .getInstance(Context.class);

        fakeGeogit = new GeoGIT(injector, workingDirectory);
        assertNotNull(fakeGeogit.getOrCreateRepository());
        command = fakeGeogit.command(ParseTimestamp.class);
    }

    @Test
    public void testWrongString() {
        command.setString("awrongstring");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid timestamp string: awrongstring");
        command.call();
        command.setString("a wrong string");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid timestamp string: a wrong string");
        command.call();
    }

    @Test
    public void testGitLikeStrings() throws ParseException {
        Date today = format.parse("1972-10-10 00:00:00");
        Date yesterday = format.parse("1972-10-09 00:00:00");
        Date aMinuteAgo = format.parse("1972-10-10 10:09:10");
        Date tenMinutesAgo = format.parse("1972-10-10 10:00:10");
        Date tenHoursTenMinutesAgo = format.parse("1972-10-10 00:00:10");
        Date aWeekAgo = format.parse("1972-10-03 10:10:10");

        Date actual;
        actual = new Date(command.setString("today").call());
        assertEquals(today, actual);
        actual = new Date(command.setString("yesterday").call());
        assertEquals(yesterday, actual);
        actual = new Date(command.setString("1.minute.ago").call());
        assertEquals(aMinuteAgo, actual);
        actual = new Date(command.setString("10.minutes.ago").call());
        assertEquals(tenMinutesAgo, actual);
        actual = new Date(command.setString("10.MINUTES.AGO").call());
        assertEquals(tenMinutesAgo, actual);
        actual = new Date(command.setString("10.hours.10.minutes.ago").call());
        assertEquals(tenHoursTenMinutesAgo, actual);
        actual = new Date(command.setString("1.week.ago").call());
        assertEquals(aWeekAgo, actual);
    }
}
