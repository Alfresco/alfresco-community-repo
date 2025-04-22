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
package org.alfresco.util.schemacomp.model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.schemacomp.DbProperty;

/**
 * Tests for the Column class.
 * 
 * @author Matt Ward
 */
@Category(BaseSpringTestsCategory.class)
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

        DbProperty thisOrderProp = new DbProperty(thisColumn, "order");
        DbProperty thatOrderProp = new DbProperty(thatColumn, "order");
        inOrder.verify(comparisonUtils).compareSimple(thisOrderProp, thatOrderProp, ctx);

        DbProperty thisAutoIncProp = new DbProperty(thisColumn, "autoIncrement");
        DbProperty thatAutoIncProp = new DbProperty(thatColumn, "autoIncrement");
        inOrder.verify(comparisonUtils).compareSimple(thisAutoIncProp, thatAutoIncProp, ctx);
    }

    @Test
    public void acceptVisitor()
    {
        thisColumn.accept(visitor);

        verify(visitor).visit(thisColumn);
    }

    @Test
    public void sameAs()
    {
        Table thisTable = new Table("the_table");
        thisColumn = new Column(thisTable, "this_column", "VARCHAR2(100)", false);

        Table thatTable = new Table("the_table");
        thatColumn = new Column(thatTable, "this_column", "VARCHAR2(100)", false);

        // This column, whilst having the same name as thisColumn, has a different
        // parent table - and so is not considered 'the same'.
        Table anotherTable = new Table("another_table");
        Column anotherColumn = new Column(anotherTable, "this_column", "VARCHAR2(100)", false);

        assertTrue("Column should always be the same as itself", thisColumn.sameAs(thisColumn));
        assertTrue("Columns should be the same due to same parent table names", thisColumn.sameAs(thatColumn));
        assertFalse("Should NOT be the same due to different parent table names", thisColumn.sameAs(anotherColumn));
    }

}
