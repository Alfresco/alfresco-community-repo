/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.attributes;

import org.alfresco.service.cmr.attributes.AttrAndQuery;
import org.alfresco.service.cmr.attributes.AttrNotQuery;
import org.alfresco.service.cmr.attributes.AttrOrQuery;
import org.alfresco.service.cmr.attributes.AttrQuery;
import org.alfresco.service.cmr.attributes.AttrQueryGT;
import org.alfresco.service.cmr.attributes.AttrQueryHelper;
import org.alfresco.service.cmr.attributes.AttrQueryLT;
import org.alfresco.service.cmr.attributes.AttrQueryLike;

import junit.framework.TestCase;

/**
 * Tests of AttrQueries.
 * @author britt
 */
public class AttrQueryTest extends TestCase
{
    private AttrQueryHelper fHelper;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        fHelper = new AttrQueryHelperImpl();
    }
    
    /**
     * Do all the testing in one.
     */
    public void testAll()
    {
        AttrQuery query = 
            new AttrOrQuery(
                new AttrAndQuery(new AttrQueryGT("cat"), new AttrQueryLT("hat")),
                new AttrNotQuery(new AttrQueryLike("fur%")));
        String predicate = query.getPredicate(fHelper);
        System.out.println(predicate);
        System.out.println(fHelper.getParameters());
        assertEquals("((me.key.key > :name0 and me.key.key < :name1) or (not me.key.key like :name2))",
                     predicate);
        assertEquals("cat", fHelper.getParameters().get("name0"));
        assertEquals("hat", fHelper.getParameters().get("name1"));
        assertEquals("fur%", fHelper.getParameters().get("name2"));
    }
}
