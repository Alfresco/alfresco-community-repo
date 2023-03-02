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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class NodeResourceHelperUnitTest
{
    @Test
    public void shouldExtractOnlyRelevantPropertiesForBeforeNode()
    {
        final Map<String, Map<String, String>> before =
                Map.of(
                        "unchanged-empty", locValues(),
                        "unchanged-non-empty", locValues("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-added", locValues("pl", "Kiełbasa"),
                        "changed-modified", locValues("pl", "XYZ", "en", "Sausage"),
                        "changed-deleted", locValues("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-added-modified-deleted", locValues("pl", "XYZ", "en", "Sausage"),
                        "changed-to-empty", locValues("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-from-empty", locValues(),
                        "removed-empty", locValues(),
                        "removed-non-empty", locValues("pl", "Kiełbasa", "en", "Sausage")
                      );

        final Map<String, Map<String, String>> after =
                Map.of(
                        "unchanged-empty", locValues(),
                        "unchanged-non-empty", locValues("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-added", locValues("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-modified", locValues("pl", "Kiełbasa", "en", "Sausage"),
                        "changed-deleted", locValues("en", "Sausage"),
                        "changed-added-modified-deleted", locValues("pl", "Kiełbasa", "de", "Würst"),
                        "changed-to-empty", locValues(),
                        "changed-from-empty", locValues("pl", "Kiełbasa", "en", "Sausage"),
                        "new-empty", locValues(),
                        "new-non-empty", locValues("de", "Würst")
                      );

        final Map<String, Map<String, String>> diff = getLocalizedPropertiesBefore(before, after);

        assertFalse(diff.containsKey("unchanged-empty"));
        assertFalse(diff.containsKey("unchanged-non-empty"));
        assertEquals(locValues("en", null), diff.get("changed-added"));
        assertEquals(locValues("pl", "XYZ"), diff.get("changed-modified"));
        assertEquals(locValues("pl", "Kiełbasa"), diff.get("changed-deleted"));
        assertEquals(locValues("pl", "XYZ", "en", "Sausage", "de", null), diff.get("changed-added-modified-deleted"));
        assertEquals(locValues("pl", "Kiełbasa", "en", "Sausage"), diff.get("changed-to-empty"));
        assertEquals(locValues("pl", null, "en", null), diff.get("changed-from-empty"));
        assertFalse(diff.containsKey("removed-empty"));
        assertEquals(locValues("pl", "Kiełbasa", "en", "Sausage"), diff.get("removed-non-empty"));
        assertFalse(diff.containsKey("new-empty"));
        assertEquals(locValues("de", null), diff.get("new-non-empty"));
    }

    private LocalizedValues locValues(String l1, String v1, String l2, String v2, String l3, String v3)
    {
        return locValues(l1, v1, l2, v2).append(l3, v3);
    }

    private LocalizedValues locValues(String l1, String v1, String l2, String v2)
    {
        return locValues(l1, v1).append(l2, v2);
    }

    private LocalizedValues locValues(String l1, String v1)
    {
        return locValues().append(l1, v1);
    }

    private LocalizedValues locValues()
    {
        return new LocalizedValues();
    }

    private static class LocalizedValues extends HashMap<String, String>
    {
        public LocalizedValues append(String language, String value)
        {
            this.put(language, value);
            return this;
        }
    }
}
