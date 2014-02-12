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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.schemacomp.DbProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * Tests for the PrimaryKey class.
 * 
 * @author Matt Ward
 */
@Category(BaseSpringTestsCategory.class)
public class PrimaryKeyTest extends DbObjectTestBase<PrimaryKey>
{
    private PrimaryKey thisPK;
    private PrimaryKey thatPK;
    private Table parent;
    
    @Before
    public void setUp()
    {
        parent = new Table("parent_table");
        
        thisPK = new PrimaryKey(
                    parent,
                    "this_pk",
                    columns("id", "name", "age"),
                    columnOrders(2, 1, 3));
        thatPK = new PrimaryKey(
                    parent,
                    "that_pk",
                    columns("a", "b"),
                    columnOrders(1, 2));        
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
        inOrder.verify(comparisonUtils).compareSimpleOrderedLists(
                    new DbProperty(thisPK, "columnNames"),
                    new DbProperty(thatPK, "columnNames"), 
                    ctx);
        inOrder.verify(comparisonUtils).compareSimpleOrderedLists(
                    new DbProperty(thisPK, "columnOrders"),
                    new DbProperty(thatPK, "columnOrders"), 
                    ctx);
    }

    @Test
    public void acceptVisitor()
    {
       thisPK.accept(visitor);
       
       verify(visitor).visit(thisPK);
    }
    
    @Test
    public void sameAs()
    {
        // Same parent, same name, same columns and ordering - must be same (intended) PK
        assertTrue("Primary keys should be logically equivalent",
                    thisPK.sameAs(
                                new PrimaryKey(
                                            parent,
                                            "this_pk",
                                            columns("id", "name", "age"),
                                            columnOrders(2, 1, 3))));
        
        // Different names - still the same PK
        assertTrue("Primary keys should be logically equivalent",
                    thisPK.sameAs(
                                new PrimaryKey(parent, "different_name",
                                columns("id", "name", "age"),
                                columnOrders(2, 1, 3))));
        
        // Same object reference ("physically" same object)
        assertTrue("PKs should be the same", thisPK.sameAs(thisPK));
        
        // Same parent, same type, but different everything else!
        assertTrue("Primary keys should be logically equivalent",
                    thisPK.sameAs(
                                new PrimaryKey(parent, "different_name",
                                columns("di", "eman", "ega"),
                                columnOrders(1, 2, 3))));
        
        // Same parent, but different type
        assertFalse("PKs are never the same as a non-PK",
                    thisPK.sameAs(new Index(parent, "wrong_type", columns())));
        
        // Different parents
        Table otherParent = new Table("other_parent");
        assertFalse("PKs should be considered different (different parents)",
                    thisPK.sameAs(
                                new PrimaryKey(otherParent, "this_pk",
                                columns("id", "name", "age"),
                                columnOrders(2, 1, 3))));
        
        // Other PK is null
        assertFalse("PKs should not be considered the same (other PK is null)",
                    thisPK.sameAs(null));
    }
    
    private List<String> columns(String... columns)
    {
        return Arrays.asList(columns);
    }

    private List<Integer> columnOrders(Integer... columnOrders)
    {
        return Arrays.asList(columnOrders);
    }
}
