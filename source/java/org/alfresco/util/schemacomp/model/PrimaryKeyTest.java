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
package org.alfresco.util.schemacomp.model;

import java.util.Arrays;

import org.alfresco.util.schemacomp.Result.Strength;
import org.junit.Before;


/**
 * Tests for the PrimaryKey class.
 * 
 * @author Matt Ward
 */
public class PrimaryKeyTest extends DbObjectTestBase<PrimaryKey>
{
    private PrimaryKey thisPK;
    private PrimaryKey thatPK;

    @Before
    public void setUp()
    {
        thisPK = new PrimaryKey("this_pk", Arrays.asList("id", "name", "age"));
        thatPK = new PrimaryKey("that_pk", Arrays.asList("a", "b"));        
    }
    
    @Override
    protected PrimaryKey getThisObject()
    {
        return thisPK;
    }

    @Override
    protected PrimaryKey getThatObject()
    {
        return thatPK;
    }

    @Override
    protected void doDiffTests()
    {
        inOrder.verify(comparisonUtils).compareSimpleCollections(
                    thisPK.getColumnNames(), 
                    thatPK.getColumnNames(), 
                    differences, 
                    Strength.ERROR);
    }

}
