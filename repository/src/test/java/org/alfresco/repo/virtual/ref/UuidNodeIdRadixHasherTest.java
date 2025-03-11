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

import static org.junit.Assert.*;

import org.junit.Test;

public class UuidNodeIdRadixHasherTest
{
    @Test
    public void testRadix36Hashing()
    {
        NodeIdHasher nodeIdHasher = new UuidNodeIdRadixHasher(36);
        String uuid = "0d3b26ff-c4c1-4680-8622-8608ea7ab4b2";

        String hashedUuid = nodeIdHasher.hash(uuid);

        String expected = "s765ou6qn3lf446dbvrkv3qq";
        assertEquals(expected, hashedUuid);
    }

    @Test
    public void testRadix36Lookup()
    {
        NodeIdHasher nodeIdHasher = new UuidNodeIdRadixHasher(36);
        String hashedUuid = "s765ou6qn3lf446dbvrkv3qq";

        String uuid = nodeIdHasher.lookup(hashedUuid);

        String expected = "0d3b26ff-c4c1-4680-8622-8608ea7ab4b2";
        assertEquals(expected, uuid);
    }

    @Test
    public void testRadix16Hashing()
    {
        NodeIdHasher nodeIdHasher = new UuidNodeIdRadixHasher(16);
        String uuid = "0d3b26ff-c4c1-4680-8622-8608ea7ab4b2";

        String hashedUuid = nodeIdHasher.hash(uuid);

        // pragma: allowlist nextline secret its just hashed random uuid
        String expected = "d3b26ffc4c1468086228608ea7ab4b2";
        assertEquals(expected, hashedUuid);
    }

    @Test
    public void testRadix16Lookup()
    {
        NodeIdHasher nodeIdHasher = new UuidNodeIdRadixHasher(16);
        // pragma: allowlist nextline secret its just hashed random uuid
        String hashedUuid = "d3b26ffc4c1468086228608ea7ab4b2";

        String uuid = nodeIdHasher.lookup(hashedUuid);

        String expected = "0d3b26ff-c4c1-4680-8622-8608ea7ab4b2";
        assertEquals(expected, uuid);
    }
}
