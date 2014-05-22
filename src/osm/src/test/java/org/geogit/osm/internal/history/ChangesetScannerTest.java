/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.osm.internal.history;

import static org.geogit.osm.internal.history.ParsingUtils.parseDateTime;
import static org.geogit.osm.internal.history.ParsingUtils.parseWGS84Bounds;

import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;
import com.vividsolutions.jts.geom.Envelope;

/**
 *
 */
public class ChangesetScannerTest extends Assert {

    /**
     * Example changeset:
     * 
     * <pre>
     * <code>
     *  <?xml version="1.0" encoding="UTF-8"?>
     *  <osm version="0.6" generator="OpenStreetMap server" copyright="OpenStreetMap and contributors" attribution="http://www.openstreetmap.org/copyright" license="http://opendatacommons.org/licenses/odbl/1-0/">
     *    <changeset id="1100" user="BMO_2009" uid="26" created_at="2009-10-10T20:02:09Z" closed_at="2009-10-10T20:02:21Z" open="false" min_lat="48.4031818" min_lon="-4.4631203" max_lat="48.4058698" max_lon="-4.4589401">
     *      <tag k="created_by" v="bulk_upload.py/17742 Python/2.5.2"/>
     *      <tag k="comment" v="second test upload of BMO data - see http://wiki.openstreetmap.org/wiki/BMO"/>
     *    </changeset>
     *  </osm>
     * </code>
     * </pre>
     * 
     * @throws XMLStreamException
     */
    @Test
    public void testParseChangeset() throws Exception {
        Changeset changeset = parse("1100.xml");
        assertNotNull(changeset);

        assertFalse(changeset.isOpen());
        assertEquals(1100L, changeset.getId());
        assertEquals(parseDateTime("2009-10-10T20:02:09Z"), changeset.getCreated());
        assertTrue(changeset.getClosed().isPresent());
        assertEquals(parseDateTime("2009-10-10T20:02:21Z"), changeset.getClosed().get().longValue());
        assertEquals(26L, changeset.getUserId());
        assertEquals("BMO_2009", changeset.getUserName());
        assertTrue(changeset.getComment().isPresent());
        assertEquals("second test upload of BMO data - see http://wiki.openstreetmap.org/wiki/BMO",
                changeset.getComment().get());
        assertEquals(ImmutableMap.of("created_by", "bulk_upload.py/17742 Python/2.5.2"),
                changeset.getTags());

        Envelope bounds = parseWGS84Bounds("48.4031818", "-4.4631203", "48.4058698", "-4.4589401");
        assertTrue(changeset.getWgs84Bounds().isPresent());
        assertEquals(bounds, changeset.getWgs84Bounds().get());
    }

    private Changeset parse(String resource) throws Exception {
        InputStream in = getClass().getResourceAsStream(resource);
        assertNotNull(in);
        try {
            Changeset changeset = new ChangesetScanner(in).parseNext();
            return changeset;
        } finally {
            Closeables.close(in, false);
        }
    }

}
