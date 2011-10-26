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


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.util.schemacomp.Result.Strength;
import org.alfresco.util.schemacomp.Result.Where;
import org.alfresco.util.schemacomp.model.DbObject;
import org.hibernate.dialect.Dialect;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the SchemaUtils class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultComparisonUtilsTest
{
    private @Mock Differences differences;
    private DefaultComparisonUtils comparisonUtils;
    private DiffContext ctx;
    private @Mock Dialect dialect;
    
    @Before
    public void setUp()
    {
        comparisonUtils = new DefaultComparisonUtils();
        ctx = new DiffContext(dialect, differences, new ArrayList<ValidationResult>());
    }
    
    @Test
    public void compareSimple()
    {
        comparisonUtils.compareSimple(null, null, ctx, Strength.ERROR);
        verify(differences).add(Where.IN_BOTH_NO_DIFFERENCE, null, null, Strength.ERROR);
        
        comparisonUtils.compareSimple("not_null_string", "not_null_string", ctx, Strength.ERROR);
        verify(differences).add(Where.IN_BOTH_NO_DIFFERENCE, "not_null_string", "not_null_string", Strength.ERROR);
        
        comparisonUtils.compareSimple("left", "right", ctx, Strength.ERROR);
        verify(differences).add(Where.IN_BOTH_BUT_DIFFERENCE, "left", "right", Strength.ERROR);
        
        comparisonUtils.compareSimple("left", null, ctx, Strength.ERROR);
        verify(differences).add(Where.ONLY_IN_LEFT, "left", null, Strength.ERROR);
        
        comparisonUtils.compareSimple(null, "right", ctx, Strength.ERROR);
        verify(differences).add(Where.ONLY_IN_RIGHT, null, "right", Strength.ERROR);
    }
    
    
    @Test
    public void compareCollections()
    {
        DbObject db1 = mock(DbObject.class);
        when(db1.sameAs(db1)).thenReturn(true);
        DbObject db2 = mock(DbObject.class); // only in left
        when(db2.sameAs(db2)).thenReturn(true);
        DbObject db3 = mock(DbObject.class); // only in right
        when(db3.sameAs(db3)).thenReturn(true);
        DbObject db4 = mock(DbObject.class);
        when(db4.sameAs(db4)).thenReturn(true);

        Collection<DbObject> left = new ArrayList<DbObject>();
        Collections.addAll(left, db1, db2, db4);
        
        Collection<DbObject> right = new ArrayList<DbObject>();
        Collections.addAll(right, db1, db3, db4);
        
        comparisonUtils.compareCollections(left, right, ctx, Strength.ERROR);
        
        // Objects in both are asked for their differences
        verify(db1).diff(db1, ctx, Strength.ERROR);
        verify(db4).diff(db4, ctx, Strength.ERROR);
        
        // Objects in only one collections are marked as such
        verify(differences).add(Where.ONLY_IN_LEFT, db2, null, Strength.ERROR);
        verify(differences).add(Where.ONLY_IN_RIGHT, null, db3, Strength.ERROR);
    }
    
    
    @Test
    public void compareSimpleCollections()
    {
        Collection<Object> leftCollection = new ArrayList<Object>();
        leftCollection.add(123);
        leftCollection.add("both");
        Collection<Object> subCollectionLeft = new ArrayList<Object>();
        subCollectionLeft.add(3);
        subCollectionLeft.add("my string");
        subCollectionLeft.add(10);
        subCollectionLeft.add("another");
        leftCollection.add(subCollectionLeft);
        leftCollection.add(456);
        leftCollection.add("left only");
        
        Collection<Object> rightCollection = new ArrayList<Object>();
        rightCollection.add(123);
        rightCollection.add(789);
        Collection<Object> subCollectionRight = new ArrayList<Object>(subCollectionLeft);
        rightCollection.add(subCollectionRight);
        rightCollection.add("right only");
        rightCollection.add("both");
        rightCollection.add("one more right only");
        
        comparisonUtils.compareSimpleCollections(leftCollection, rightCollection, ctx, Strength.WARN);
        
        verify(differences).add(Where.IN_BOTH_NO_DIFFERENCE, 123, 123, Strength.WARN);
        verify(differences).add(Where.IN_BOTH_NO_DIFFERENCE, "both", "both", Strength.WARN);
        verify(differences).add(Where.IN_BOTH_NO_DIFFERENCE, subCollectionLeft, subCollectionRight, Strength.WARN);
        verify(differences).add(Where.ONLY_IN_LEFT, 456, null, Strength.WARN);
        verify(differences).add(Where.ONLY_IN_LEFT, "left only", null, Strength.WARN);

        verify(differences).add(Where.ONLY_IN_RIGHT, null, 789, Strength.WARN);
        verify(differences).add(Where.ONLY_IN_RIGHT, null, "right only", Strength.WARN);
        verify(differences).add(Where.ONLY_IN_RIGHT, null, "one more right only", Strength.WARN);
    }
    
    
    @Test
    public void findSameObjectAsSuccessfulFind()
    {
        // Make a list of mock DbOjbect objects to test against
        int numObjects = 20;
        List<DbObject> dbObjects = createMockDbObjects(numObjects);
        
        // The reference that will be used to look for one 'the same' in the collection.
        DbObject toFind = mock(DbObject.class);
        
        // For all other objects sameAs() will return false
        DbObject objShouldBeFound = dbObjects.get(12);
        when(objShouldBeFound.sameAs(toFind)).thenReturn(true);
        
        DbObject found = comparisonUtils.findSameObjectAs(dbObjects, toFind);
        
        assertSame("Found the wrong DbObject", objShouldBeFound, found);
    }
    
    
    @Test
    public void findSameObjectAsNotFound()
    {
        // Make a list of mock DbOjbect objects to test against
        int numObjects = 20;
        List<DbObject> dbObjects = createMockDbObjects(numObjects);
        
        // The reference that will be used to look for one 'the same' in the collection.
        DbObject toFind = mock(DbObject.class);
        
        DbObject found = comparisonUtils.findSameObjectAs(dbObjects, toFind);
        
        assertNull("Should not have found a matching DbObject", found);
    }
    
    private List<DbObject> createMockDbObjects(int size)
    {
        ArrayList<DbObject> dbObjects = new ArrayList<DbObject>(size);
        for (int i = 0; i < size; i++)
        {
            DbObject dbo = mock(DbObject.class);
            when(dbo.toString()).thenReturn("Mock DbObject " + i);
            dbObjects.add(dbo);
        }
        return dbObjects;
    }
}
