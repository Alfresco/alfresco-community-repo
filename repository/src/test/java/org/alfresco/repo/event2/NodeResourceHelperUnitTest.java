/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import static org.alfresco.repo.event2.NodeResourceHelper.getLocalizedPropertiesBefore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.junit.Test;

public class NodeResourceHelperUnitTest
{
    @Test
    public void shouldExtractOnlyRelevantPropertiesForBeforeNode()
    {
        final Map<String, Map<String, String>> before =
                Map.of(
                        "unchanged-empty", Map.of(),
                        "unchanged-non-empty", Map.of("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-added", Map.of("pl", "Kiełbasa"),
                        "changed-modified", Map.of("pl", "XYZ", "en", "Sausage"),
                        "changed-deleted", Map.of("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-added-modified-deleted", Map.of("pl", "XYZ", "en", "Sausage"),
                        "changed-to-empty", Map.of("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-from-empty", Map.of(),
                        "removed-empty", Map.of(),
                        "removed-non-empty", Map.of("pl", "Kiełbasa", "en", "Sausage")
                      );

        final Map<String, Map<String, String>> after =
                Map.of(
                        "unchanged-empty", Map.of(),
                        "unchanged-non-empty", Map.of("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-added", Map.of("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-modified", Map.of("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-deleted", Map.of("en", "Sausage"),
                        "changed-added-modified-deleted", Map.of("pl", "Kiełbasa", "de", "Würst"),
                        "changed-to-empty", Map.of(),
                        "changed-from-empty", Map.of("pl", "Kiełbasa", "en", "Sausage"),
                        "new-empty", Map.of(),
                        "new-non-empty", Map.of("de", "Würst")
                      );

        final Map<String, Map<String, String>> diff = getLocalizedPropertiesBefore(before, after);

        assertFalse(diff.containsKey("unchanged-empty"));
        assertFalse(diff.containsKey("unchanged-non-empty"));
        assertEquals(Map.of(), diff.get("changed-added"));
        assertEquals(Map.of("pl", "XYZ"), diff.get("changed-modified"));
        assertEquals(Map.of("pl", "Kiełbasa"), diff.get("changed-deleted"));
        assertEquals(Map.of("pl", "XYZ", "en", "Sausage"), diff.get("changed-added-modified-deleted"));
        assertEquals(Map.of("pl", "Kiełbasa", "en", "Sausage"), diff.get("changed-to-empty"));
        assertEquals(Map.of(), diff.get("changed-from-empty"));
        assertEquals(Map.of(), diff.get("removed-empty"));
        assertEquals(Map.of("pl", "Kiełbasa", "en", "Sausage"), diff.get("removed-non-empty"));
        assertFalse(diff.containsKey("new-empty"));
        assertFalse(diff.containsKey("new-non-empty"));
    }
}
