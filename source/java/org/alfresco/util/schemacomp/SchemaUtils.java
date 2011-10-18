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

import org.alfresco.util.schemacomp.Result.Where;
import org.alfresco.util.schemacomp.model.DbObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 * TODO: comment me!
 * 
 * @author Matt Ward
 */
public abstract class SchemaUtils
{
    /**
     * @param objToFind
     * @return
     */
    public static DbObject findSameObjectAs(Collection<? extends DbObject> objects,
                final DbObject objToFind)
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

    public static void compareSimpleCollections(Collection<? extends Object> leftCollection,
                Collection<? extends Object> rightCollection, Differences differences)
    {
        for (Object leftObj : leftCollection)
        {
            if (rightCollection.contains(leftObj))
            {
                // The same valued object in the right hand collection as in the left.
                differences.add(Where.IN_BOTH_NO_DIFFERENCE, leftObj, leftObj);
            }
            else
            {
                // No equivalent object in the right hand collection.
                differences.add(Where.ONLY_IN_LEFT, leftObj, null);
            }
        }

        // Identify objects in the right collection but not the left
        for (Object rightObj : rightCollection)
        {
            if (!leftCollection.contains(rightObj))
            {
                // No equivalent object in the left hand collection.
                differences.add(Where.ONLY_IN_RIGHT, null, rightObj);
            }
        }
    }

    public static void compareCollections(Collection<? extends DbObject> leftCollection,
                Collection<? extends DbObject> rightCollection, Differences differences)
    {
        for (DbObject leftObj : leftCollection)
        {
            DbObject rightObj = SchemaUtils.findSameObjectAs(rightCollection, leftObj);

            if (rightObj != null)
            {
                // There is an equivalent object in the right hand collection as
                // in the left.
                leftObj.diff(rightObj, differences);
            }
            else
            {
                // No equivalent object in the right hand collection.
                differences.add(Where.ONLY_IN_LEFT, leftObj, null);
            }
        }

        // Identify objects in the right collection but not the left
        for (DbObject rightObj : rightCollection)
        {
            if (!leftCollection.contains(rightObj))
            {
                // No equivalent object in the left hand collection.
                differences.add(Where.ONLY_IN_RIGHT, null, rightObj);
            }
        }
    }

    public static void compareSimple(Object left, Object right, Differences differences)
    {
        if (left == null && right == null)
        {
            differences.add(Where.IN_BOTH_NO_DIFFERENCE, null, null);
        }
        else if (left != null && left.equals(right))
        {
            differences.add(Where.IN_BOTH_NO_DIFFERENCE, left, right);
        }
        else if (left == null && right != null)
        {
            differences.add(Where.ONLY_IN_RIGHT, null, right);
        }
        else if (left != null && right == null)
        {
            differences.add(Where.ONLY_IN_LEFT, left, null);
        }
    }
}
