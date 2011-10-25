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

import java.util.Collection;

import org.alfresco.util.schemacomp.Result.Strength;
import org.alfresco.util.schemacomp.Result.Where;
import org.alfresco.util.schemacomp.model.DbObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 * A collection of utility methods for determining differences between two database schemas.
 * 
 * @author Matt Ward
 */
public class DefaultComparisonUtils implements ComparisonUtils
{
    /**
     * @param objToFind
     * @return
     */
    @Override
    public DbObject findSameObjectAs(Collection<? extends DbObject> objects, final DbObject objToFind)
    {
        return (DbObject) CollectionUtils.find(objects, new Predicate()
        {
            @Override
            public boolean evaluate(Object o)
            {
                DbObject object = (DbObject) o;
                return object.sameAs(objToFind);
            }
        });
    }

    
    @Override
    public void compareSimpleCollections(Collection<? extends Object> leftCollection,
                Collection<? extends Object> rightCollection, DiffContext ctx, Strength strength)
    {
        Differences differences = ctx.getDifferences();
        for (Object leftObj : leftCollection)
        {
            if (rightCollection.contains(leftObj))
            {
                // The same valued object in the right hand collection as in the left.
                // Note: it isn't possible to determine a result of Where.IN_BOTH_BUT_DIFFERENCE
                // with a 'simple' value — as there is no way of knowing if the term represents the same value
                // (e.g. two strings {red_value, green_value}, are these meant to be the same or different?)
                differences.add(Where.IN_BOTH_NO_DIFFERENCE, leftObj, leftObj, strength);
            }
            else
            {
                // No equivalent object in the right hand collection.
                differences.add(Where.ONLY_IN_LEFT, leftObj, null, strength);
            }
        }

        // Identify objects in the right collection but not the left
        for (Object rightObj : rightCollection)
        {
            if (!leftCollection.contains(rightObj))
            {
                // No equivalent object in the left hand collection.
                differences.add(Where.ONLY_IN_RIGHT, null, rightObj, strength);
            }
        }
    }

    /**
     * Compare collections, reporting differences using the default {@link Result.Strength}
     * 
     * @see #compareCollections(Collection, Collection, Differences, Strength)
     */
    @Override
    public void compareCollections(Collection<? extends DbObject> leftCollection,
                Collection<? extends DbObject> rightCollection, DiffContext ctx)
    {
        compareCollections(leftCollection, rightCollection, ctx, null);
    }
    
    /**
     * Compare collections of {@link DbObject}s using their {@link DbObject#diff(DbObject, Differences)} method.
     * Differences are reported using the specified {@link Result.Strength}.
     * 
     * @param leftCollection
     * @param rightCollection
     * @param differences
     * @param strength
     */
    @Override
    public void compareCollections(Collection<? extends DbObject> leftCollection,
                Collection<? extends DbObject> rightCollection, DiffContext ctx, Strength strength)
    {
        Differences differences = ctx.getDifferences();
        for (DbObject leftObj : leftCollection)
        {
            DbObject rightObj = findSameObjectAs(rightCollection, leftObj);

            if (rightObj != null)
            {
                // There is an equivalent object in the right hand collection as
                // in the left.
                leftObj.diff(rightObj, ctx, strength);
            }
            else
            {
                // No equivalent object in the right hand collection.
                differences.add(Where.ONLY_IN_LEFT, leftObj, null, strength);
            }
        }

        // Identify objects in the right collection but not the left
        for (DbObject rightObj : rightCollection)
        {
            if (!leftCollection.contains(rightObj))
            {
                // No equivalent object in the left hand collection.
                differences.add(Where.ONLY_IN_RIGHT, null, rightObj, strength);
            }
        }
    }

    /**
     * Compare two simple objects. Differences are reported using the default Result.Strength.
     * 
     * @see #compareSimple(Object, Object, Differences, Strength)
     */
    @Override
    public void compareSimple(Object left, Object right, DiffContext ctx)
    {
        compareSimple(left, right, ctx, null);
    }
    
    /**
     * Compare two 'simple' (i.e. non-{@link DbObject} objects) using their {@link Object#equals(Object)} method
     * to decide if there is a difference. Differences are reported using the Result.Strength specified.
     * 
     * @param left
     * @param right
     * @param differences
     * @param strength
     */
    @Override
    public void compareSimple(Object left, Object right, DiffContext ctx, Strength strength)
    {
        
        Where where = null;
        
        if (left == right)
        {
            // Same object, or both nulls
            where = Where.IN_BOTH_NO_DIFFERENCE;
        }
        else if (left == null)
        {
            // right can't be null, or left == right would have been true
            where = Where.ONLY_IN_RIGHT;
        }
        else if (right == null)
        {
            // left can't be null, or left == right would have been true
            where = Where.ONLY_IN_LEFT;
        }
        else
        {
            // neither are null
            if (left.equals(right))
            {
                where = Where.IN_BOTH_NO_DIFFERENCE;
            }
            else
            {
                where = Where.IN_BOTH_BUT_DIFFERENCE;
            }
        }
        
        ctx.getDifferences().add(where, left, right, strength);
    }
}
