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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.util.schemacomp.Difference.Where;
import org.alfresco.util.schemacomp.model.AbstractDbObject;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.validator.DbValidator;
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
    private @Mock Results differences;
    private DefaultComparisonUtils comparisonUtils;
    private DiffContext ctx;
    private @Mock Dialect dialect;
    
    @Before
    public void setUp()
    {
        comparisonUtils = new DefaultComparisonUtils();
        ctx = new DiffContext(dialect, differences, null, null);
    }
    
    @Test
    public void compareSimple()
    {
        comparisonUtils.compareSimple(prop(null), prop(null), ctx);
        verify(differences).add(Where.IN_BOTH_NO_DIFFERENCE, prop(null), prop(null));
        
        comparisonUtils.compareSimple(prop("not_null_string"), prop("not_null_string"), ctx);
        verify(differences).add(Where.IN_BOTH_NO_DIFFERENCE, prop("not_null_string"), prop("not_null_string"));
        
        comparisonUtils.compareSimple(prop("Not_Null_String"), prop("NOT_NULL_STRING"), ctx);
        verify(differences).add(Where.IN_BOTH_NO_DIFFERENCE, prop("Not_Null_String"), prop("NOT_NULL_STRING"));
        
        comparisonUtils.compareSimple(prop("left"), prop("right"), ctx);
        verify(differences).add(Where.IN_BOTH_BUT_DIFFERENCE, prop("left"), prop("right"));
        
        comparisonUtils.compareSimple(prop("left"), prop(null), ctx);
        verify(differences).add(Where.ONLY_IN_REFERENCE, prop("left"), prop(null));
        
        comparisonUtils.compareSimple(prop(null), prop("right"), ctx);
        verify(differences).add(Where.ONLY_IN_TARGET, prop(null), prop("right"));
    }
    
    public DbProperty prop(String propValue)
    {
        DbObject dbo = new DbObjectWithCollection("dbo", null);
        return dbPropForValue(dbo, "someProperty", propValue);
    }
    
    @Test
    public void compareCollections()
    {
        DbObject db1 = new DatabaseObject("db1");
        DbObject db2 = new DatabaseObject("db2"); // only in left
        DbObject db3 = new DatabaseObject("db3"); // only in right
        DbObject db4 = new DatabaseObject("db4");
        

        Collection<DbObject> left = new ArrayList<DbObject>();
        Collections.addAll(left, db1, db2, db4);
        
        Collection<DbObject> right = new ArrayList<DbObject>();
        Collections.addAll(right, db1, db3, db4);
        
        comparisonUtils.compareCollections(left, right, ctx);
        
        // Differences and ommissions are noticed...
        verify(differences).add(Where.IN_BOTH_BUT_DIFFERENCE, new DbProperty(db1), new DbProperty(db1));
        verify(differences).add(Where.ONLY_IN_REFERENCE, new DbProperty(db2), null);
        verify(differences).add(Where.ONLY_IN_TARGET, null, new DbProperty(db3));
        verify(differences).add(Where.IN_BOTH_BUT_DIFFERENCE, new DbProperty(db4), new DbProperty(db4));
    }
    
    @Test
    public void compareCollectionsWithMultipleMatches()
    {
        DbObject db2 = new DatabaseObject("db2");
        DbObject db3 = new DatabaseObject("db3");
        DbObject db4 = new DatabaseObject("db4");
        DbObject db1 = new DatabaseObject("db1", db2, db3);
        
        Collection<DbObject> left = new ArrayList<DbObject>();
        Collections.addAll(left, db1, db4);
        
        Collection<DbObject> right = new ArrayList<DbObject>();
        Collections.addAll(right, db1, db2, db3);
        
        comparisonUtils.compareCollections(left, right, ctx);
        
        // Differences and ommissions are noticed...
        verify(differences).add(Where.ONLY_IN_REFERENCE, new DbProperty(db4), null);
        verify(differences).add(Where.IN_BOTH_BUT_DIFFERENCE, new DbProperty(db1), new DbProperty(db1));
        verify(differences).add(Where.IN_BOTH_BUT_DIFFERENCE, new DbProperty(db1), new DbProperty(db2));
        verify(differences).add(Where.IN_BOTH_BUT_DIFFERENCE, new DbProperty(db1), new DbProperty(db3));
    }
    
    @Test
    public void propertyIsNotComparedWhenValidatorTakesResponsibility()
    {
        DbObject db1 = new DatabaseObject("db1");
        DbProperty db1NameProp = new DbProperty(db1, "name");
        DbObject db2 = new DatabaseObject("db2");
        DbProperty db2NameProp = new DbProperty(db2, "name");
        
        // Using mock to decouple unit test from actual NameValidator.
        DbValidator nameValidator = mock(DbValidator.class);
        when(nameValidator.validates("name")).thenReturn(true);
        db1.getValidators().add(nameValidator);
        
        comparisonUtils.compareSimple(db1NameProp, db2NameProp, ctx);
        
        verify(differences, never()).add(Where.IN_BOTH_BUT_DIFFERENCE, db1NameProp, db2NameProp);
    }
    
    @Test
    public void collectionPropertyIsNotComparedWhenValidatorTakesResponsibility()
    {
        Collection<Object> refCollection = new ArrayList<Object>();
        refCollection.add(123);
        refCollection.add("both");
        
        DbObject refDbObj = new DbObjectWithCollection("left", refCollection);
        DbProperty refCollProp = new DbProperty(refDbObj, "collection");
        
        Collection<Object> targetCollection = new ArrayList<Object>();
        targetCollection.add(234);
        targetCollection.add("both");
        
        DbObject targetDbObj = new DbObjectWithCollection("right", targetCollection);
        DbProperty targetCollProp = new DbProperty(targetDbObj, "collection");
        
     
        DbValidator validator = mock(DbValidator.class);
        when(validator.validates("collection")).thenReturn(true);
        refDbObj.getValidators().add(validator);
        
        comparisonUtils.compareSimpleCollections(refCollProp, targetCollProp, ctx);
        
        
        // No information should be reported...
        verify(differences, never()).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(refDbObj, "collection[1]", "both"),
                    dbPropForValue(targetDbObj, "collection[1]", "both"));
        
        verify(differences, never()).add(
                    Where.ONLY_IN_REFERENCE,
                    dbPropForValue(refDbObj, "collection[0]", 123),
                    dbPropForValue(targetDbObj, "collection", targetCollection));

        verify(differences, never()).add(
                    Where.ONLY_IN_TARGET,
                    dbPropForValue(refDbObj, "collection", refCollection),
                    dbPropForValue(targetDbObj, "collection[0]", 234));
    }
    
    @Test
    public void objectIsNotComparedWhenValidatorTakesResponsibility()
    {
        DbObject db1 = new DatabaseObject("db1");
        DbObject db2 = new DatabaseObject("db2");
        DbObject db3 = new DatabaseObject("db3");
        DbObject db4 = new DatabaseObject("db4");
        
        Collection<DbObject> reference = new ArrayList<DbObject>();
        Collections.addAll(reference, db1, db3);
        
        Collection<DbObject> target = new ArrayList<DbObject>();
        Collections.addAll(target, db2, db4);
        
        DbValidator validator = mock(DbValidator.class);
        when(validator.validatesFullObject()).thenReturn(true);
        db1.getValidators().add(validator);
        db2.getValidators().add(validator);
        
        comparisonUtils.compareCollections(reference, target, ctx);
        
        verify(differences, never()).add(Where.ONLY_IN_REFERENCE, new DbProperty(db1), null);
        verify(differences, never()).add(Where.ONLY_IN_TARGET, null, new DbProperty(db2));
        verify(differences).add(Where.ONLY_IN_REFERENCE, new DbProperty(db3), null);
        verify(differences).add(Where.ONLY_IN_TARGET, null, new DbProperty(db4));
    }
    
    // Check that two 'simple' collections (i.e. composed of non DbObject objects)
    // must have the same items at the same indexes for those items to qualify as being in both
    // collections with no difference. This is important for, e.g. column orderings in
    // indexes or primary keys.
    @Test
    public void orderingFaultsWhenCollectionsSameSize()
    {
        Collection<Object> left =  new ArrayList<Object>();
        Collections.<Object>addAll(left, 0, "one", 2, "3", "4_this", 5, "6_this", "seven");
        DbObject leftDbObj = new DbObjectWithCollection("left", left);
        DbProperty leftCollProp = new DbProperty(leftDbObj, "collection");
        
        Collection<Object> right =  new ArrayList<Object>();
        Collections.<Object>addAll(right, 0, "one", 2, "3", "4_that", 5, "6_that", "seven");        
        DbObject rightDbObj = new DbObjectWithCollection("right", right);
        DbProperty rightCollProp = new DbProperty(rightDbObj, "collection");
        
        comparisonUtils.compareSimpleOrderedLists(leftCollProp, rightCollProp, ctx);
        
        
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE, 
                    dbPropForValue(leftDbObj, "collection[0]", 0), 
                    dbPropForValue(rightDbObj, "collection[0]", 0));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[1]", "one"),
                    dbPropForValue(rightDbObj, "collection[1]", "one"));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[2]", 2),
                    dbPropForValue(rightDbObj, "collection[2]", 2));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[3]", "3"),
                    dbPropForValue(rightDbObj, "collection[3]", "3"));
        verify(differences).add(
                    Where.IN_BOTH_BUT_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[4]", "4_this"),
                    dbPropForValue(rightDbObj, "collection[4]", "4_that"));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[5]", 5),
                    dbPropForValue(rightDbObj, "collection[5]", 5));
        verify(differences).add(
                    Where.IN_BOTH_BUT_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[6]", "6_this"),
                    dbPropForValue(rightDbObj, "collection[6]", "6_that"));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[7]", "seven"),
                    dbPropForValue(rightDbObj, "collection[7]", "seven"));
    }
    
    @Test
    public void orderingFaultsWhenReferenceCollectionLonger()
    {    
        Collection<Object> reference =  new ArrayList<Object>();
        Collections.<Object>addAll(reference, "a", "z", "x", "1", "2");
        DbObject refDbObj = new DbObjectWithCollection("reference", reference);
        DbProperty refCollProp = new DbProperty(refDbObj, "collection");
        
        Collection<Object> target =  new ArrayList<Object>();
        Collections.<Object>addAll(target, "a", "Q", "x");        
        DbObject targetDbObj = new DbObjectWithCollection("target", target);
        DbProperty targetCollProp = new DbProperty(targetDbObj, "collection");
        
        comparisonUtils.compareSimpleOrderedLists(refCollProp, targetCollProp, ctx);
        
        
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE, 
                    dbPropForValue(refDbObj, "collection[0]", "a"), 
                    dbPropForValue(targetDbObj, "collection[0]", "a"));
        verify(differences).add(
                    Where.IN_BOTH_BUT_DIFFERENCE,
                    dbPropForValue(refDbObj, "collection[1]", "z"),
                    dbPropForValue(targetDbObj, "collection[1]", "Q"));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(refDbObj, "collection[2]", "x"),
                    dbPropForValue(targetDbObj, "collection[2]", "x"));
        verify(differences).add(
                    Where.ONLY_IN_REFERENCE,
                    dbPropForValue(refDbObj, "collection[3]", "1"),
                    null);
        verify(differences).add(
                    Where.ONLY_IN_REFERENCE,
                    dbPropForValue(refDbObj, "collection[4]", "2"),
                    null);
    }
    
    @Test
    public void orderingFaultsWhenTargetCollectionLonger()
    {    
        Collection<Object> reference =  new ArrayList<Object>();
        Collections.<Object>addAll(reference, "a", "z", "x");
        DbObject refDbObj = new DbObjectWithCollection("reference", reference);
        DbProperty refCollProp = new DbProperty(refDbObj, "collection");
        
        Collection<Object> target =  new ArrayList<Object>();
        Collections.<Object>addAll(target, "a", "Q", "x", "1", "2");        
        DbObject targetDbObj = new DbObjectWithCollection("target", target);
        DbProperty targetCollProp = new DbProperty(targetDbObj, "collection");
        
        comparisonUtils.compareSimpleOrderedLists(refCollProp, targetCollProp, ctx);
        
        
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE, 
                    dbPropForValue(refDbObj, "collection[0]", "a"), 
                    dbPropForValue(targetDbObj, "collection[0]", "a"));
        verify(differences).add(
                    Where.IN_BOTH_BUT_DIFFERENCE,
                    dbPropForValue(refDbObj, "collection[1]", "z"),
                    dbPropForValue(targetDbObj, "collection[1]", "Q"));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(refDbObj, "collection[2]", "x"),
                    dbPropForValue(targetDbObj, "collection[2]", "x"));
        verify(differences).add(
                    Where.ONLY_IN_TARGET,
                    null,
                    dbPropForValue(targetDbObj, "collection[3]", "1"));
        verify(differences).add(
                    Where.ONLY_IN_TARGET,
                    null,
                    dbPropForValue(targetDbObj, "collection[4]", "2"));
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
        DbObject leftDbObj = new DbObjectWithCollection("left", leftCollection);
        DbProperty leftCollProp = new DbProperty(leftDbObj, "collection");
        
        Collection<Object> rightCollection = new ArrayList<Object>();
        rightCollection.add(123);
        rightCollection.add(789);
        Collection<Object> subCollectionRight = new ArrayList<Object>(subCollectionLeft);
        rightCollection.add(subCollectionRight);
        rightCollection.add("right only");
        rightCollection.add("both");
        rightCollection.add("one more right only");
        DbObject rightDbObj = new DbObjectWithCollection("right", rightCollection);
        DbProperty rightCollProp = new DbProperty(rightDbObj, "collection");
        
        comparisonUtils.compareSimpleCollections(leftCollProp, rightCollProp, ctx);
        
        
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE, 
                    dbPropForValue(leftDbObj, "collection[0]", 123), 
                    dbPropForValue(rightDbObj, "collection[0]", 123));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[1]", "both"),
                    dbPropForValue(rightDbObj, "collection[4]", "both"));
        verify(differences).add(
                    Where.IN_BOTH_NO_DIFFERENCE,
                    dbPropForValue(leftDbObj, "collection[2]", subCollectionLeft),
                    dbPropForValue(rightDbObj, "collection[2]", subCollectionRight));
        verify(differences).add(
                    Where.ONLY_IN_REFERENCE,
                    dbPropForValue(leftDbObj, "collection[3]", 456),
                    dbPropForValue(rightDbObj, "collection", rightCollection));
        verify(differences).add(
                    Where.ONLY_IN_REFERENCE,
                    dbPropForValue(leftDbObj, "collection[4]", "left only"),
                    dbPropForValue(rightDbObj, "collection", rightCollection));

        verify(differences).add(
                    Where.ONLY_IN_TARGET,
                    dbPropForValue(leftDbObj, "collection", leftCollection),
                    dbPropForValue(rightDbObj, "collection[1]", 789));
        verify(differences).add(
                    Where.ONLY_IN_TARGET,
                    dbPropForValue(leftDbObj, "collection", leftCollection),
                    dbPropForValue(rightDbObj, "collection[3]", "right only"));
        verify(differences).add(
                    Where.ONLY_IN_TARGET,
                    dbPropForValue(leftDbObj, "collection", leftCollection),
                    dbPropForValue(rightDbObj, "collection[5]", "one more right only"));
    }
    
    private DbProperty dbPropForValue(DbObject obj, String propName, Object propValue)
    {
        return new DbProperty(obj, propName, -1, true, propValue);
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
    
    
    public static class DbObjectWithCollection extends AbstractDbObject
    {
        private Collection<Object> collection;
        
        public DbObjectWithCollection(String name, Collection<Object> collection)
        {
            super(null, name);
            this.collection = collection;
        }

        @Override
        public void accept(DbObjectVisitor visitor)
        {
        }

        public Collection<Object> getCollection()
        {
            return this.collection;
        }
    }
    
    
    public static class DatabaseObject extends AbstractDbObject
    {
        private DbObject[] equivalentObjects = new DbObject[] {};
        
        public DatabaseObject(String name)
        {
            super(null, name);
        }

        public DatabaseObject(String name, DbObject... equivalentObjects)
        {
            this(name);
            this.equivalentObjects = equivalentObjects;
        }
        
        @Override
        public void accept(DbObjectVisitor visitor)
        {
            visitor.visit(this);
        }

        @Override
        protected void doDiff(DbObject right, DiffContext ctx)
        {
            DbProperty leftProp = new DbProperty(this);
            DbProperty rightProp = new DbProperty(right);
            ctx.getComparisonResults().add(Where.IN_BOTH_BUT_DIFFERENCE, leftProp, rightProp);
        }

        @Override
        public boolean sameAs(DbObject other)
        {
            // We can tell this stub to treat certain other objects as 'the same' as this object
            // by supplying them in the constructor. If this object is invoked with t.sameAs(o)
            // and o is in the list of equivalent objects supplied in the constructor, then
            // sameAs() will return true. Otherwise the default sameAs() implementation is used.
            for (DbObject o : equivalentObjects)
            {
                if (other.equals(o))
                {
                    return true;
                }
            }
            return super.sameAs(other);
        }
    }
}
