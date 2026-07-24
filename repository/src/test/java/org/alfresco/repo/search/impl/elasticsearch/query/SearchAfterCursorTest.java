/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.search.impl.elasticsearch.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opensearch.client.opensearch._types.FieldValue;

import org.alfresco.repo.search.impl.elasticsearch.query.SearchAfterCursor.Decoded;

public class SearchAfterCursorTest
{
    @Test
    public void shouldRoundTripPitIdAndSortValues()
    {
        String pitId = "pit-abc-123";
        List<FieldValue> sort = List.of(FieldValue.of("2024-01-01T00:00:00Z"), FieldValue.of("abc-123-uuid"));

        String cursor = SearchAfterCursor.encode(pitId, sort);
        Decoded decoded = SearchAfterCursor.decode(cursor);

        assertEquals(pitId, decoded.pitId());
        assertSortEquals(sort, decoded.sort());
    }

    @Test
    public void shouldEncodeNothingAsNull()
    {
        assertNull(SearchAfterCursor.encode(null, List.of()));
        assertNull(SearchAfterCursor.encode("", List.of()));
        assertNull(SearchAfterCursor.encode(null, null));
    }

    @Test
    public void shouldEncodePitIdEvenWithoutSortValues()
    {
        String cursor = SearchAfterCursor.encode("pit-1", List.of());

        assertNotNull(cursor);
        Decoded decoded = SearchAfterCursor.decode(cursor);
        assertEquals("pit-1", decoded.pitId());
        assertTrue(decoded.sort().isEmpty());
    }

    @Test
    public void shouldDecodeNullOrBlankAsEmpty()
    {
        assertNull(SearchAfterCursor.decode(null).pitId());
        assertTrue(SearchAfterCursor.decode(null).sort().isEmpty());
        assertNull(SearchAfterCursor.decode("").pitId());
        assertTrue(SearchAfterCursor.decode("   ").sort().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidCursor()
    {
        SearchAfterCursor.decode("@@@not-a-valid-cursor@@@");
    }

    @Test
    public void shouldPreserveSortValueTypes()
    {
        // A keyword value stays a string, while the epoch-millis date and the _shard_doc tiebreaker stay numeric;
        // coercing the numeric values to strings would break search_after because OpenSearch compares by type.
        List<FieldValue> sort = List.of(
                FieldValue.of("text/plain"),
                FieldValue.of(1704067200000L),
                FieldValue.of(42L));

        Decoded decoded = SearchAfterCursor.decode(SearchAfterCursor.encode("pit-1", sort));
        List<FieldValue> roundTripped = decoded.sort();

        assertEquals(3, roundTripped.size());
        assertTrue("keyword value must stay a string", roundTripped.get(0).isString());
        assertEquals("text/plain", roundTripped.get(0).stringValue());
        assertTrue("date value must stay numeric", roundTripped.get(1).isLong());
        assertEquals(1704067200000L, roundTripped.get(1).longValue());
        assertTrue("_shard_doc tiebreaker must stay numeric", roundTripped.get(2).isLong());
        assertEquals(42L, roundTripped.get(2).longValue());
    }

    private static void assertSortEquals(List<FieldValue> expected, List<FieldValue> actual)
    {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++)
        {
            assertEquals(expected.get(i)._toJsonString(), actual.get(i)._toJsonString());
        }
    }
}
