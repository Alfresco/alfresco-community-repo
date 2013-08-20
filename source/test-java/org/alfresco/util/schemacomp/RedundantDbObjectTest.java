/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.schemacomp.model.AbstractDbObject;
import org.alfresco.util.schemacomp.model.DbObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Tests for the {@link RedundantDbObject} class.
 * 
 * @author Matt Ward
 */
public class RedundantDbObjectTest
{
    @Before
    public void setUp()
    {
        I18NUtil.registerResourceBundle("alfresco.messages.system-messages");
    }
    
    @Test
    public void describe()
    {
        DbObject reference = new MyDbObject("reference");
        List<DbObject> matches = makeMatches(3);
        
        RedundantDbObject redundantDBO = new RedundantDbObject(reference, matches);
        
        assertEquals("Redundancy: 3 items matching MyDbObject[name=reference], " +
                    "matches: MyDbObject[name=match1], MyDbObject[name=match2], MyDbObject[name=match3]",
                    redundantDBO.describe());
    }

    @Test
    public void describeTooManyMatches()
    {
        DbObject reference = new MyDbObject("reference");
        List<DbObject> matches = makeMatches(4);
        
        RedundantDbObject redundantDBO = new RedundantDbObject(reference, matches);
        
        assertEquals("4 redundant items? reference: MyDbObject[name=reference], " +
                    "matches: MyDbObject[name=match1], MyDbObject[name=match2], MyDbObject[name=match3] and 1 more...",
                    redundantDBO.describe());
    }

    /**
     * @return
     */
    private List<DbObject> makeMatches(int numMatches)
    {
        List<DbObject> matches = new ArrayList<DbObject>();
        for (int i = 0; i < numMatches; i++)
        {
            matches.add(new MyDbObject("match" + (i+1)));
        }
        return matches;
    }
    
    
    private static class MyDbObject extends AbstractDbObject
    {
        public MyDbObject(String name)
        {
            super(null, name);
        }

        @Override
        public void accept(DbObjectVisitor visitor)
        {
        }   
    }
}
