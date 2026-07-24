/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.opensearch._types.FieldValue;

// Encodes and decodes the opaque cursor, a Base64-encoded JSON blob holding the PIT id and the last page's sort values {"pitId":..., "sort":[...]}. The client then sends nextCursor back as searchAfter, and the PIT id is refreshed from each response.
public final class SearchAfterCursor
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SearchAfterCursor()
    {}

    /**
     * Encodes the PIT id and sort values into an opaque cursor token.
     *
     * @param pitId
     *            the (latest) Point-In-Time id for the pagination session
     * @param sortValues
     *            the last hit's typed sort values
     * @return the opaque cursor, or {@code null} when there is nothing to carry forward
     */
    public static String encode(String pitId, List<FieldValue> sortValues)
    {
        boolean noPit = pitId == null || pitId.isBlank();
        boolean noSort = sortValues == null || sortValues.isEmpty();
        if (noPit && noSort)
        {
            return null;
        }
        try
        {
            List<Object> rawSort = noSort ? List.of() : sortValues.stream().map(SearchAfterCursor::toRawValue).toList();
            byte[] json = MAPPER.writeValueAsBytes(new Payload(pitId, rawSort));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        }
        catch (Exception exception)
        {
            throw new IllegalStateException("Unable to encode search_after cursor", exception);
        }
    }

    public static Decoded decode(String cursor)
    {
        if (cursor == null || cursor.isBlank())
        {
            return new Decoded(null, List.of());
        }
        try
        {
            byte[] json = Base64.getUrlDecoder().decode(cursor);
            Payload payload = MAPPER.readValue(json, Payload.class);
            List<FieldValue> sort = new ArrayList<>();
            if (payload.sort() != null)
            {
                payload.sort().forEach(raw -> sort.add(toFieldValue(raw)));
            }
            return new Decoded(payload.pitId(), List.copyOf(sort));
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Invalid search_after cursor", exception);
        }
    }

    private static Object toRawValue(FieldValue fieldValue)
    {
        if (fieldValue == null || fieldValue.isNull())
        {
            return null;
        }
        if (fieldValue.isLong())
        {
            return fieldValue.longValue();
        }
        if (fieldValue.isDouble())
        {
            return fieldValue.doubleValue();
        }
        if (fieldValue.isBoolean())
        {
            return fieldValue.booleanValue();
        }
        return fieldValue.stringValue();
    }

    // Rebuild a typed FieldValue from the plain JSON value produced by {@link #toRawValue}.
    private static FieldValue toFieldValue(Object raw)
    {
        if (raw == null)
        {
            return FieldValue.NULL;
        }
        if (raw instanceof Boolean booleanValue)
        {
            return FieldValue.of(booleanValue);
        }
        if (raw instanceof Double || raw instanceof Float)
        {
            return FieldValue.of(((Number) raw).doubleValue());
        }
        if (raw instanceof Number number)
        {
            return FieldValue.of(number.longValue());
        }
        return FieldValue.of(raw.toString());
    }

    public record Decoded(String pitId, List<FieldValue> sort)
    {}

    private record Payload(@JsonProperty("pitId") String pitId, @JsonProperty("sort") List<Object> sort)
    {}
}
