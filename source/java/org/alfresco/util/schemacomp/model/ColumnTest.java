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


import org.alfresco.util.schemacomp.DbProperty;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 * Tests for the Column class.
 * @author Matt Ward
 */
public class ColumnTest extends DbObjectTestBase<Column>
{
    private Column thisColumn;
    private Column thatColumn;
    
    
    @Before
    public void setUp() throws Exception
    {
        thisColumn = new Column(null, "this_column", "VARCHAR2(100)", false);
        thatColumn = new Column(null, "that_column", "NUMBER(10)", true);
    }

    @Override
    protected Column getThisObject()
    {
        return thisColumn;
    }

    @Override
    protected Column getThatObject()
    {
        return thatColumn;
    }

    @Override
    protected void doDiffTests()
    {
        DbProperty thisTypeProp = new DbProperty(thisColumn, "type");
        DbProperty thatTypeProp = new DbProperty(thatColumn, "type");
        inOrder.verify(comparisonUtils).compareSimple(thisTypeProp, thatTypeProp, ctx);
        
        DbProperty thisNullableProp = new DbProperty(thisColumn, "nullable");
        DbProperty thatNullableProp = new DbProperty(thatColumn, "nullable");
        
        inOrder.verify(comparisonUtils).compareSimple(thisNullableProp, thatNullableProp, ctx);
    }
    
    @Test
    public void acceptVisitor()
    {
       thisColumn.accept(visitor);
       
       verify(visitor).visit(thisColumn);
    }

}
