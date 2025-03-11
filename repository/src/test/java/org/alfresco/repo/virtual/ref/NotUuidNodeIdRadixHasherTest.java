/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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

package org.alfresco.repo.virtual.ref;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NotUuidNodeIdRadixHasherTest
{
    @Test
    public void testRadix36Hashing()
    {
        NodeIdHasher nodeIdHasher = new NotUuidNodeIdRadixHasher(36);
        String id = "wf-email-html-ftl";

        String hashedId = nodeIdHasher.hash(id);

        String expected = "1e9lat6m0tvszgcle5scyylab8s";
        assertEquals(expected, hashedId);
    }

    @Test
    public void testRadix36Lookup()
    {
        NodeIdHasher nodeIdHasher = new NotUuidNodeIdRadixHasher(36);
        String hashedUuid = "1e9lat6m0tvszgcle5scyylab8s";

        String id = nodeIdHasher.lookup(hashedUuid);

        String expected = "wf-email-html-ftl";
        assertEquals(expected, id);
    }

    @Test
    public void testRadix16Hashing()
    {
        NodeIdHasher nodeIdHasher = new NotUuidNodeIdRadixHasher(16);
        String id = "wf-email-html-ftl";

        String hashedId = nodeIdHasher.hash(id);

        String expected = "77662d656d61696c2d68746d6c2d66746c";
        assertEquals(expected, hashedId);
    }

    @Test
    public void testRadix16Lookup()
    {
        NodeIdHasher nodeIdHasher = new NotUuidNodeIdRadixHasher(16);
        String hashedUuid = "77662d656d61696c2d68746d6c2d66746c";

        String id = nodeIdHasher.lookup(hashedUuid);

        String expected = "wf-email-html-ftl";
        assertEquals(expected, id);
    }
}
