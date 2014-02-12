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

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.schemacomp.DbProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the Schema class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
@Category(BaseSpringTestsCategory.class)
public class SchemaTest extends DbObjectTestBase<Schema>
{
    private Schema left;
    private Schema right;
    
    @Before
    public void setUp()
    {
        left = new Schema("left_schema");
        right = new Schema("right_schema");
    }
    
    @Override
    protected Schema getThisObject()
    {
        return left;
    }

    @Override
    protected Schema getThatObject()
    {
        return right;
    }

    @Override
    protected void doDiffTests()
    {
        // We need to be warned if comparing, for example a version 500 schema with a
        // version 501 schema.
        inOrder.verify(comparisonUtils).compareSimple(
                    new DbProperty(left, "version"),
                    new DbProperty(right, "version"),
                    ctx);
        
        // In addition to the base class functionality, Schema.diff() compares
        // the DbObjects held in the other schema with its own DbObjects.
        inOrder.verify(comparisonUtils).compareCollections(left.objects, right.objects, ctx);
    }
    
    @Test
    public void acceptVisitor()
    {
       DbObject dbo1 = Mockito.mock(DbObject.class);
       left.add(dbo1);
       DbObject dbo2 = Mockito.mock(DbObject.class);
       left.add(dbo2);
       DbObject dbo3 = Mockito.mock(DbObject.class);
       left.add(dbo3);
       
       left.accept(visitor);
       
       verify(dbo1).accept(visitor);
       verify(dbo2).accept(visitor);
       verify(dbo3).accept(visitor);
       verify(visitor).visit(left);
    }
    
    @Test
    public void sameAs()
    {
        // We have to assume that two schemas are always the same, regardless of name,
        // otherwise unless the reference schema has the same name as the target database
        // all the comparisons will fail - and users can choose to install databases with any schema
        // name they choose.
        assertTrue("Schemas should be considered the same", left.sameAs(right));

        // Things are always the same as themselves.
        assertTrue("Schemas are the same physical object", left.sameAs(left));
        
        assertFalse("A table is not the same as a schema", left.sameAs(new Table("left_schema")));
        assertFalse("null is not the same as a schema", left.sameAs(null));
    }
}
