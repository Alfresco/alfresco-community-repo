/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import junit.framework.TestCase;

public class StringPairTest extends TestCase
{
    private final StringPair pairA = new StringPair("name", "Fred");
    private final StringPair pairAduplicate = new StringPair("name", "Fred");
    private final StringPair pairB = new StringPair("name", "Barney");

    public StringPairTest(String name)
    {
        super(name);
    }

    public void testNullValuedPair()
    {
        StringPair nullValue = new StringPair("NullValue", null);
        
        // These calls are to generate potential NPEs.
        int ignored = nullValue.hashCode();
        boolean dontCare = nullValue.equals(pairA);
        dontCare = pairA.equals(nullValue);
        ignored = nullValue.compareTo(pairA);
        ignored = pairA.compareTo(nullValue);
    }
    
    public void testHashCode()
    {
        assertEquals("Equal objects must have equal hashCodes.", pairA.hashCode(), pairAduplicate.hashCode());
    }

    public void testEquals()
    {
        assertEquals("Equal objects appear unequal.", pairA, pairAduplicate);
        assertTrue("Unequal objects appear equal.", !pairA.equals(pairB));
    }

    public void testCompareTo()
    {
        assertEquals("Equal objects have compareTo == 0.", 0, pairA.compareTo(pairAduplicate));
        assertEquals("Equal objects have compareTo == 0.", 0, pairAduplicate.compareTo(pairA));

        assertTrue("Unequal objects should have compareTo != 0.", pairA.compareTo(pairB) > 0);
        assertTrue("Unequal objects should have compareTo != 0.", pairB.compareTo(pairA) < 0);
    }

    public void testCompareToNull()
    {
        try
        {
            pairA.compareTo(null);
        }
        catch (NullPointerException expected)
        {
            return;
        }
        fail("Expected NPE not thrown.");
    }
}
