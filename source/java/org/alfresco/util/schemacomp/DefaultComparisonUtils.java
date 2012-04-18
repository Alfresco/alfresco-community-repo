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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.util.schemacomp.Difference.Where;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.validator.DbValidator;

/**
 * A collection of utility methods for determining differences between two database schemas.
 * 
 * @author Matt Ward
 */
public class DefaultComparisonUtils implements ComparisonUtils
{    
    @Override
    public List<DbObject> findEquivalentObjects(DbObject rootObject, DbObject objToMatch)
    {
        EquivalentObjectSeeker objectSeeker = new EquivalentObjectSeeker(objToMatch);
        rootObject.accept(objectSeeker);
        
        return objectSeeker.getMatches();
    }



    @Override
    public void compareSimpleOrderedLists(DbProperty refProp, DbProperty targetProp, DiffContext ctx)
    {
        checkPropertyContainsList(refProp);
        checkPropertyContainsList(targetProp);
        
        // Check whether the leftProperty should be compared to the rightProperty 
        DbObject leftDbObject = refProp.getDbObject();
        if (leftDbObject.hasValidators())
        {
            for (DbValidator validator : leftDbObject.getValidators())
            {
                if (validator.validates(refProp.getPropertyName()))
                {
                    // Don't perform differencing on this property - a validator will handle it.
                    return;
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        ArrayList<Object> refList = new ArrayList<Object>((List<Object>) refProp.getPropertyValue());
        @SuppressWarnings("unchecked")
        ArrayList<Object> targetList = new ArrayList<Object>((List<Object>) targetProp.getPropertyValue());
        
        Results differences = ctx.getComparisonResults();

        int maxSize = Math.max(refList.size(), targetList.size());
        
        for (int i = 0; i < maxSize; i++)
        {
            if (i < refList.size() && i < targetList.size())
            {
                DbProperty refIndexedProp = new DbProperty(refProp.getDbObject(), refProp.getPropertyName(), i);   
                DbProperty targetIndexedProp = new DbProperty(targetProp.getDbObject(), targetProp.getPropertyName(), i);
                
                if (refList.get(i).equals(targetList.get(i)))
                {
                    differences.add(Where.IN_BOTH_NO_DIFFERENCE, refIndexedProp, targetIndexedProp);
                }
                else
                {
                    differences.add(Where.IN_BOTH_BUT_DIFFERENCE, refIndexedProp, targetIndexedProp);                    
                }
            }
            else if (i < refList.size())
            {
                DbProperty indexedProp = new DbProperty(refProp.getDbObject(), refProp.getPropertyName(), i);
                differences.add(Where.ONLY_IN_REFERENCE, indexedProp, null);
            }
            else
            {
                DbProperty indexedProp = new DbProperty(targetProp.getDbObject(), targetProp.getPropertyName(), i);
                // No equivalent object in the reference collection.
                differences.add(Where.ONLY_IN_TARGET, null, indexedProp);
            }
        }
    }

    /**
     * Ensure the property is carrying a list as its payload. A List is required
     * rather than a Collection as the latter may not be ordered.
     * 
     * @param prop
     */
    private void checkPropertyContainsList(DbProperty prop)
    {
        if (!List.class.isAssignableFrom(prop.getPropertyValue().getClass()))
        {
            throw new IllegalArgumentException("List required, but was " + prop.getPropertyValue().getClass());
        }
    }
    
    @Override
    public void compareSimpleCollections(DbProperty leftProp,
                DbProperty rightProp, DiffContext ctx)
    {
        // Check whether the leftProperty should be compared to the rightProperty 
        DbObject leftDbObject = leftProp.getDbObject();
        if (leftDbObject.hasValidators())
        {
            for (DbValidator validator : leftDbObject.getValidators())
            {
                if (validator.validates(leftProp.getPropertyName()))
                {
                    // Don't perform differencing on this property - a validator will handle it.
                    return;
                }
            }
        }
        @SuppressWarnings("unchecked")
        Collection<? extends Object> leftCollection = (Collection<? extends Object>) leftProp.getPropertyValue();
        @SuppressWarnings("unchecked")
        Collection<? extends Object> rightCollection = (Collection<? extends Object>) rightProp.getPropertyValue();
        
        ArrayList<? extends Object> leftList = new ArrayList<Object>(leftCollection);
        ArrayList<? extends Object> rightList = new ArrayList<Object>(rightCollection);
        
        Results differences = ctx.getComparisonResults();

        for (int leftIndex = 0; leftIndex < leftList.size(); leftIndex++)
        {
            Object leftObj = leftList.get(leftIndex);
            DbProperty leftIndexedProp = new DbProperty(leftProp.getDbObject(), leftProp.getPropertyName(), leftIndex);
            
            int rightIndex;
            if ((rightIndex = rightList.indexOf(leftObj)) != -1)
            {
                // The same valued object in the right hand collection as in the left.
                // Note: it isn't possible to determine a result of Where.IN_BOTH_BUT_DIFFERENCE
                // with a 'simple' value — as there is no way of knowing if the term represents the same value
                // (e.g. two strings {red_value, green_value}, are these meant to be the same or different?)
                DbProperty rightIndexedProp = new DbProperty(rightProp.getDbObject(), rightProp.getPropertyName(), rightIndex);
                differences.add(Where.IN_BOTH_NO_DIFFERENCE, leftIndexedProp, rightIndexedProp);
            }
            else
            {
                // No equivalent object in the right hand collection.
                // Using rightIndexedProperty would result in index out of bounds error.
                differences.add(Where.ONLY_IN_REFERENCE, leftIndexedProp, rightProp);
            }
        }

        // Identify objects in the right collection but not the left
        for (int rightIndex = 0; rightIndex < rightList.size(); rightIndex++)
        {
            Object rightObj = rightList.get(rightIndex);
            if (!leftCollection.contains(rightObj))
            {
                DbProperty rightIndexedProp = new DbProperty(rightProp.getDbObject(), rightProp.getPropertyName(), rightIndex);
                // No equivalent object in the left hand collection.
                differences.add(Where.ONLY_IN_TARGET, leftProp, rightIndexedProp);
            }
        }
    }

    
    @Override
    public void compareCollections(Collection<? extends DbObject> leftCollection,
                Collection<? extends DbObject> rightCollection, DiffContext ctx)
    {
        Results differences = ctx.getComparisonResults();
        for (DbObject leftObj : leftCollection)
        {    
            if (leftObj.hasObjectLevelValidator())
            {
                // Don't report differences regarding this object - there is a validator
                // that takes sole responsibility for doing so.
                continue;
            }
        
            boolean foundMatch = false;
            
            for (DbObject rootObject : rightCollection)
            {                
                List<DbObject> matches = findEquivalentObjects(rootObject, leftObj);
                
                for (DbObject match : matches)
                {                        
                    // There is an equivalent object in the right hand collection as in the left.
                    leftObj.diff(match, ctx);
                }
                
                if (matches.size() > 0)
                {
                    foundMatch = true;
                }
            }
            
            if (!foundMatch)
            {
                // No equivalent object in the target collection.
                differences.add(Where.ONLY_IN_REFERENCE, new DbProperty(leftObj, null), null);
            }
        }

        // Identify objects in the right collection but not the left
        for (DbObject rightObj : rightCollection)
        {
            if (rightObj.hasObjectLevelValidator())
            {
                // Don't report differences regarding this object - there is a validator
                // that takes sole responsibility for doing so.
                continue;
            }
            
            boolean foundMatch = false;
            
            for (DbObject rootObject : leftCollection)
            {
                List<DbObject> matches = findEquivalentObjects(rootObject, rightObj);
                if (matches.size() > 0)
                {
                    foundMatch = true;
                    break;
                }
            }
            
            if (!foundMatch)
            {
                // No equivalent object in the left hand collection.
                differences.add(Where.ONLY_IN_TARGET, null, new DbProperty(rightObj, null));
            }
        }
    }

    
    @Override
    public void compareSimple(DbProperty leftProperty, DbProperty rightProperty, DiffContext ctx)
    {
        // Check whether the leftProperty should be compared to the rightProperty 
        DbObject leftDbObject = leftProperty.getDbObject();
        if (leftDbObject.hasValidators())
        {
            for (DbValidator validator : leftDbObject.getValidators())
            {
                if (validator.validates(leftProperty.getPropertyName()))
                {
                    // Don't perform differencing on this property - a validator will handle it.
                    return;
                }
            }
        }
        
        
        Where where = null;
        
        Object left = leftProperty.getPropertyValue();
        checkNotDbObject(left);
        Object right = rightProperty.getPropertyValue();
        checkNotDbObject(right);
        
        if (left == right)
        {
            // Same object, or both nulls
            where = Where.IN_BOTH_NO_DIFFERENCE;
        }
        else if (left == null)
        {
            // right can't be null, or left == right would have been true
            where = Where.ONLY_IN_TARGET;
        }
        else if (right == null)
        {
            // left can't be null, or left == right would have been true
            where = Where.ONLY_IN_REFERENCE;
        }
        else
        {
            // neither are null
            boolean objectsAreEqual;
            // Strings are compared case-insensitively, e.g. table names.
            if (left instanceof String && right instanceof String)
            {
                objectsAreEqual = ((String) left).equalsIgnoreCase((String) right);
            }
            else
            {
                objectsAreEqual = left.equals(right);
            }
            
            if (objectsAreEqual)
            {
                where = Where.IN_BOTH_NO_DIFFERENCE;
            }
            else
            {
                where = Where.IN_BOTH_BUT_DIFFERENCE;
            }
        }
        
        ctx.getComparisonResults().add(where, leftProperty, rightProperty);
    }


    /**
     * @param obj
     */
    private void checkNotDbObject(Object obj)
    {
        if (obj != null && DbObject.class.isAssignableFrom(obj.getClass()))
        {
            throw new IllegalArgumentException(
                        "Property value is a DbObject - this method shouldn't be used to compare this type: " + obj);
        }
    }
    
    
    public static class EquivalentObjectSeeker implements DbObjectVisitor
    {
        private final List<DbObject> matches = new ArrayList<DbObject>();
        private final DbObject objToMatch;
        
        public EquivalentObjectSeeker(DbObject objToMatch)
        {
            this.objToMatch = objToMatch;
        }
        
        @Override
        public void visit(DbObject dbObject)
        {
            if (objToMatch.sameAs(dbObject))
            {
                matches.add(dbObject);
            }
        }

        /**
         * @return the matches
         */
        public List<DbObject> getMatches()
        {
            return this.matches;
        }
    }
}
