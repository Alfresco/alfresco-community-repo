/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 Alfresco Software Limited
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
package org.alfresco.repo.version;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;

import org.alfresco.service.cmr.version.Version;

public final class VersionTestUtil
{
    private VersionTestUtil()
    {}

    public static void assertVersions(Version expectedVersion, Version actualVersion)
    {
        assertEquals("FrozenStateNodeRefs are not the same",
                expectedVersion.getFrozenStateNodeRef(),
                actualVersion.getFrozenStateNodeRef());
        assertEquals("VersionedNodeRefs are not the same",
                expectedVersion.getVersionedNodeRef(),
                actualVersion.getVersionedNodeRef());
        assertEquals("Versionlabels are not the same",
                expectedVersion.getVersionLabel(),
                actualVersion.getVersionLabel());
    }

    public static void assertVersions(Collection<Version> expected, Collection<Version> actual)
    {
        assertEquals("Version collections are of different size", expected.size(), actual.size());
        Iterator<Version> expectedIt = expected.iterator();
        Iterator<Version> actualIt = actual.iterator();
        while (expectedIt.hasNext() && actualIt.hasNext())
        {
            Version expectedVersion = expectedIt.next();
            Version actualVersion = actualIt.next();
            assertVersions(expectedVersion, actualVersion);
        }
    }
}
