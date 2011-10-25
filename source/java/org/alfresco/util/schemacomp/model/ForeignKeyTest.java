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


import org.junit.Before;

/**
 * Tests for the ForeignKey class.
 * 
 * @author Matt Ward
 */
public class ForeignKeyTest extends DbObjectTestBase<ForeignKey>
{
    private ForeignKey thisFK, thatFK;
    

    @Before
    public void setUp() throws Exception
    {
        thisFK = new ForeignKey("this_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey("that_fk", "local_col", "target_table", "target_col");
    }


    @Override
    protected ForeignKey getThisObject()
    {
        return thisFK;
    }


    @Override
    protected ForeignKey getThatObject()
    {
        return thatFK;
    }


    @Override
    protected void doDiffTests()
    {
        inOrder.verify(comparisonUtils).compareSimple(thisFK.getLocalColumn(), thatFK.getLocalColumn(), differences);
        inOrder.verify(comparisonUtils).compareSimple(thisFK.getTargetTable(), thatFK.getTargetTable(), differences);
        inOrder.verify(comparisonUtils).compareSimple(thisFK.getTargetColumn(), thatFK.getTargetColumn(), differences);
    }
}
