/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
