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


import static org.mockito.Mockito.verify;

import org.alfresco.util.schemacomp.DbProperty;
import org.junit.Before;
import org.junit.Test;

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
        thisFK = new ForeignKey(null, "this_fk", "local_col", "target_table", "target_col");
        thatFK = new ForeignKey(null, "that_fk", "local_col", "target_table", "target_col");
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
        inOrder.verify(comparisonUtils).compareSimple(
                    new DbProperty(thisFK, "localColumn"),
                    new DbProperty(thatFK, "localColumn"),
                    ctx);
        inOrder.verify(comparisonUtils).compareSimple(
                    new DbProperty(thisFK, "targetTable"),
                    new DbProperty(thatFK, "targetTable"),
                    ctx);
        inOrder.verify(comparisonUtils).compareSimple(
                    new DbProperty(thisFK, "targetColumn"),
                    new DbProperty(thatFK, "targetColumn"),
                    ctx);
    }
    
    @Test
    public void acceptVisitor()
    {
       thisFK.accept(visitor);
       
       verify(visitor).visit(thisFK);
    }
}
