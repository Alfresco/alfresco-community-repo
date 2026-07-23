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

import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

// Encodes/decodes the opaque cursor a Base64 JSON blob holding the PIT id and the last page's sort values ({@code {"pitId":..., "sort":[...]}}). Clients just sends nextCursor back as searchAfter the PIT id is refreshed from each response.
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
     *            the last hit's sort values
     * @return the opaque cursor, or {@code null} when there is nothing to carry forward
     */
    public static String encode(String pitId, List<String> sortValues)
    {
        boolean noPit = pitId == null || pitId.isBlank();
        boolean noSort = sortValues == null || sortValues.isEmpty();
        if (noPit && noSort)
        {
            return null;
        }
        try
        {
            byte[] json = MAPPER.writeValueAsBytes(new Payload(pitId, noSort ? List.of() : sortValues));
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
            List<String> sort = payload.sort() == null ? List.of() : payload.sort();
            return new Decoded(payload.pitId(), sort);
        }
        catch (Exception exception)
        {
            throw new IllegalArgumentException("Invalid search_after cursor", exception);
        }
    }

    public record Decoded(String pitId, List<String> sort)
    {}

    private record Payload(@JsonProperty("pitId") String pitId, @JsonProperty("sort") List<String> sort)
    {}
}
