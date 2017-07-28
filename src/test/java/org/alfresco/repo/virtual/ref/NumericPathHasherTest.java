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

public class NumericPathHasherTest extends TestCase
{
    @Test
    public void testHash() throws Exception
    {
        NumericPathHasher nph = new NumericPathHasher();

        Pair<String, String> h1 = nph.hash("/1/2/3");
        assertEquals(new Pair<String, String>("123",
                                              null),
                     h1);

        Pair<String, String> h2 = nph.hash("/a/2/3");
        String xc2 = new String(Base64.encodeBase64("/a/2/3".getBytes(),
                                                    false));
        assertEquals(new Pair<String, String>(null,
                                              xc2),
                     h2);

        Pair<String, String> h3 = nph.hash("/1/2/a/1/3");
        String xc3 = new String(Base64.encodeBase64("a/1/3".getBytes(),
                                                    false));
        assertEquals(new Pair<String, String>("12",
                                              xc3),
                     h3);

    }

    @Test
    public void testHashRoot() throws Exception
    {
        NumericPathHasher nph = new NumericPathHasher();

        Pair<String, String> h1 = nph.hash("/");
        assertEquals(new Pair<String, String>(null,
                                              ""),
                     h1);
    }

    @Test
    public void testLookupRoot() throws Exception
    {
        NumericPathHasher nph = new NumericPathHasher();

        String p = nph.lookup(new Pair<String, String>(null,
                                                       ""));
        assertEquals("/",
                     p);
    }

    @Test
    public void testLookup() throws Exception
    {
        NumericPathHasher nph = new NumericPathHasher();
        Pair<String, String> h = nph.hash("/1/2/3");

        assertEquals(new Pair<String, String>("123",
                                              null),
                     h);

        String xc2 = new String(Base64.encodeBase64("/a/2/3".getBytes(),
                                                    false));
        String p2 = nph.lookup(new Pair<String, String>(null,
                                                        xc2));
        assertEquals("/a/2/3",
                     p2);

        String xc3 = new String(Base64.encodeBase64("a/1/3".getBytes(),
                                                    false));
        String p3 = nph.lookup(new Pair<String, String>("12",
                                                        xc3));
        assertEquals("/1/2/a/1/3",
                     p3);
    }

    @Test
    public void testHashEmpty() throws Exception
    {
        NumericPathHasher nph = new NumericPathHasher();

        try
        {
            nph.hash("");
            fail("Should not be able to hash empty String");
        }
        catch (IllegalArgumentException e)
        {
            // as expected
        }
    }
}
