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

import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.Result.Strength;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;


/**
 * Tests for the Index class.
 * @author Matt Ward
 */
public class IndexTest extends DbObjectTestBase<Index>
{
    private Index thisIndex;
    private Index thatIndex;

    @Before
    public void setUp()
    {
        thisIndex = new Index(null, "this_index", Arrays.asList("id", "name", "age"));
        thatIndex = new Index(null, "that_index", Arrays.asList("a", "b"));        
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
        inOrder.verify(comparisonUtils).compareSimpleCollections(
                    new DbProperty(thisIndex, "columnNames"), 
                    new DbProperty(thatIndex, "columnNames"),
                    ctx, 
                    Strength.ERROR);
        inOrder.verify(comparisonUtils).compareSimple(
                    new DbProperty(thisIndex, "unique"),
                    new DbProperty(thatIndex, "unique"),
                    ctx);
    }

    
    @Test
    public void differentNamesResultInWarningsNotErrors()
    {
        assertEquals("Name differences should be reported as warnings.", Strength.WARN, thisIndex.getNameStrength());
    }
    
    
    @Test
    public void sameAs()
    {
       assertTrue("Indexes should be logically the same.",
                   thisIndex.sameAs(new Index(null, "this_index", Arrays.asList("id", "name", "age"))));
       
       assertTrue("Indexes should be logically the same, despite different names (as same column order)",
                   thisIndex.sameAs(new Index(null, "different_name", Arrays.asList("id", "name", "age")))); 

       assertTrue("Indexes should be identified as the same despite different column order (as same name).",
                   thisIndex.sameAs(new Index(null, "this_index", Arrays.asList("name", "id", "age")))); 

       assertFalse("Indexes should be identified different (different name and column order)",
                   thisIndex.sameAs(new Index(null, "different_name", Arrays.asList("name", "id", "age")))); 
       
       assertFalse("Indexes should be identified different (different name & different columns)",
                   thisIndex.sameAs(new Index(null, "different_name", Arrays.asList("node_ref", "url"))));
    }
    
    
    @Test
    public void acceptVisitor()
    {
       thisIndex.accept(visitor);
       
       verify(visitor).visit(thisIndex);
    }
}
