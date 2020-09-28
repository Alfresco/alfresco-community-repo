/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import junit.framework.TestCase;

import org.alfresco.util.Pair;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class StoredPathHasherTest extends TestCase
{
    private HashStore hashStore;

    public void setUp()
    {
        hashStore = new HashStore();
        hashStore.put("/com/alfresco",
                      "1");
    }

    @Test
    public void testHash() throws Exception
    {

        hashStore.put("/org/alfresco",
                      "2");

        StoredPathHasher hasher = new StoredPathHasher(hashStore);

        assertEquals(new Pair<String, String>("1",
                                              null),
                     hasher.hash("/com/alfresco/"));

        assertEquals(new Pair<String, String>("1",
                                              null),
                     hasher.hash("/com/alfresco"));

        assertEquals(new Pair<String, String>(null,
                                              Base64.encodeBase64String("/com".getBytes())),
                     hasher.hash("/com"));

        assertEquals(new Pair<String, String>(null,
                                              Base64.encodeBase64String("/com".getBytes())),
                     hasher.hash("/com/"));

        assertEquals(new Pair<String, String>("1",
                                              Base64.encodeBase64String("foo/bar".getBytes())),
                     hasher.hash("/com/alfresco/foo/bar"));

        assertEquals(new Pair<String, String>("2",
                                              Base64.encodeBase64String("foo/bar".getBytes())),
                     hasher.hash("/org/alfresco/foo/bar"));
    }

    @Test
    public void testHashRoot() throws Exception
    {
        StoredPathHasher hasher = new StoredPathHasher(hashStore);

        assertEquals(new Pair<String, String>(null,
                                              ""),
                     hasher.hash("/"));
    }

    @Test
    public void testNullPaths() throws Exception
    {

        {

            StoredPathHasher hasher = new StoredPathHasher(hashStore);

            try
            {
                hasher.hash("");
                fail("Shold not be able to hash empty paths.");
            }
            catch (IllegalArgumentException e)
            {
                // as expected
            }
        }

        {

            StoredPathHasher hasher = new StoredPathHasher(hashStore);

            try
            {
                hasher.hash(null);
                fail("Shold not be able to hash null paths.");
            }
            catch (IllegalArgumentException e)
            {
                // as expected
            }
        }
    }

}
