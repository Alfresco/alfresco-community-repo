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
import java.util.List;

import org.alfresco.util.schemacomp.model.DbObject;

import com.google.gdata.data.extensions.Where;

/**
 * Utilities for comparing data structures in the context of comparing two database schemas.
 * 
 * @author Matt Ward
 */
public interface ComparisonUtils
{
    List<DbObject> findEquivalentObjects(DbObject rootObject, DbObject objToMatch);
    
    
    /**
     * Compare two {@link List}s of 'simple' (i.e. non-{@link DbObject}) objects. Ordering
     * is significant - if an element E appears in both collections but at different indexes
     * then it is not considered to be the same item.
     * 
     * @param leftProperty
     * @param rightProperty
     * @param ctx
     */
    void compareSimpleOrderedLists(DbProperty leftProperty, DbProperty rightProperty, DiffContext ctx);

    /**
     * Compare two collections. Similar to {@link #compareSimpleOrderedLists(DbProperty, DbProperty, DiffContext)}
     * except that this method operates on {@link Collection}s and order (and cardinality) is not important. If
     * an element E from the reference collection appears one or more times at any position in the target collection
     * then that element is said to be {@link Where#IN_BOTH_NO_DIFFERENCE in both with no difference}.
     * 
     * @param leftProperty
     * @param rightProperty
     * @param ctx
     */
    void compareSimpleCollections(DbProperty leftProperty, DbProperty rightProperty, DiffContext ctx);
    
    /**
     * Compare collections of {@link DbObject}s using their {@link DbObject#diff(DbObject, Differences)} method.
     * 
     * @param leftCollection
     * @param rightCollection
     * @param differences
     */
    void compareCollections(Collection<? extends DbObject> leftCollection,
                Collection<? extends DbObject> rightCollection, DiffContext ctx);


    /**
     * Compare two 'simple' (i.e. non-{@link DbObject}) objects using their {@link Object#equals(Object)} method
     * to decide if there is a difference.
     * 
     * @param left
     * @param right
     * @param differences
     */
    void compareSimple(DbProperty left, DbProperty right, DiffContext ctx);

}