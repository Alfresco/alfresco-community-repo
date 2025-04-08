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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.schemacomp.DbProperty;

/**
 * Tests for the Index class.
 * 
 * @author Matt Ward
 */
@Category(BaseSpringTestsCategory.class)
public class IndexTest extends DbObjectTestBase<Index>
{
    private Table thisTable;
    private Index thisIndex;
    private Table thatTable;
    private Index thatIndex;

    @Before
    public void setUp()
    {
        thisTable = new Table("this_table");
        thisIndex = new Index(thisTable, "this_index", Arrays.asList("id", "name", "age"));
        thatTable = new Table("that_table");
        thatIndex = new Index(thatTable, "that_index", Arrays.asList("a", "b"));
    }

    @Override
    protected Index getThisObject()
    {
        return thisIndex;
    }

    @Override
    protected Index getThatObject()
    {
        return thatIndex;
    }

    @Override
    protected void doDiffTests()
    {
        inOrder.verify(comparisonUtils).compareSimpleOrderedLists(
                new DbProperty(thisIndex, "columnNames"),
                new DbProperty(thatIndex, "columnNames"),
                ctx);
        inOrder.verify(comparisonUtils).compareSimple(
                new DbProperty(thisIndex, "unique"),
                new DbProperty(thatIndex, "unique"),
                ctx);
    }

    @Test
    public void sameAs()
    {
        assertTrue("Indexes should be logically the same.",
                thisIndex.sameAs(new Index(thisTable, "this_index", Arrays.asList("id", "name", "age"))));

        assertFalse("Indexes have logically different parents",
                thisIndex.sameAs(new Index(thatTable, "this_index", Arrays.asList("id", "name", "age"))));

        assertTrue("Indexes should be logically the same, despite different names (as same column order)",
                thisIndex.sameAs(new Index(thisTable, "different_name", Arrays.asList("id", "name", "age"))));

        assertTrue("Indexes should be identified as the same despite different column order (as same name).",
                thisIndex.sameAs(new Index(thisTable, "this_index", Arrays.asList("name", "id", "age"))));

        assertFalse("Indexes should be identified different (different name and column order)",
                thisIndex.sameAs(new Index(thisTable, "different_name", Arrays.asList("name", "id", "age"))));

        assertFalse("Indexes should be identified different (different name & different columns)",
                thisIndex.sameAs(new Index(thisTable, "different_name", Arrays.asList("node_ref", "url"))));
    }

    @Test
    public void acceptVisitor()
    {
        thisIndex.accept(visitor);

        verify(visitor).visit(thisIndex);
    }
}
