/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search;

import java.util.Iterator;

import org.jaxen.UnsupportedAxisException;
import junit.framework.TestCase;

public class DocumentNavigatorTest extends TestCase
{
    public void testGetChildAxisIterator() throws UnsupportedAxisException
    {
        try
        {
            // Check true
            DocumentNavigator docNav = new DocumentNavigator(null, null, null, null, false, false);
            Iterator nodeIter = docNav.getChildAxisIterator(null, "true", "", null);
            assertNotNull(nodeIter);
            boolean value = (Boolean) nodeIter.next();
            assertTrue("The true value should be returned. See MNT-10730", value);

            // Check false
            nodeIter = docNav.getChildAxisIterator(null, "false", "", null);
            assertNotNull(nodeIter);
            value = (Boolean) nodeIter.next();
            assertFalse("The false value should be returned. See MNT-10730", value);
        }
        catch (NullPointerException exp)
        {
            fail("The boolean value should be returned. See MNT-10730");
        }
    }
}