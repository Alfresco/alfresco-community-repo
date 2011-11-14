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

import org.alfresco.util.schemacomp.Result.Strength;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Schema;

/**
 * Utilities for comparing data structures in the context of comparing two database schemas.
 * 
 * @author Matt Ward
 */
public interface ComparisonUtils
{
    /**
     * @deprecated This method ignores the fact that multiple objects may match.
     * @param objToFind
     * @return
     */
    DbObject findSameObjectAs(Collection<? extends DbObject> objects, final DbObject objToFind);

    List<DbObject> findEquivalentObjects(Schema schema, DbObject objToMatch);
    
    void compareSimpleCollections(DbProperty leftProperty, DbProperty rightProperty,
                DiffContext ctx, Strength strength);

    /**
     * Compare collections, reporting differences using the default {@link Difference.Strength}
     * 
     * @see #compareCollections(Collection, Collection, Differences, Strength)
     */
    void compareCollections(Collection<? extends DbObject> leftCollection,
                Collection<? extends DbObject> rightCollection, DiffContext ctx);

    /**
     * Compare collections of {@link DbObject}s using their {@link DbObject#diff(DbObject, Differences)} method.
     * Differences are reported using the specified {@link Difference.Strength}.
     * 
     * @param leftCollection
     * @param rightCollection
     * @param differences
     * @param strength
     */
    void compareCollections(Collection<? extends DbObject> leftCollection,
                Collection<? extends DbObject> rightCollection, DiffContext ctx,
                Strength strength);

    /**
     * Compare two simple objects. Differences are reported using the default Result.Strength.
     * 
     * @see #compareSimple(Object, Object, Differences, Strength)
     */
    void compareSimple(DbProperty left, DbProperty right, DiffContext ctx);

    /**
     * Compare two 'simple' (i.e. non-{@link DbObject} objects) using their {@link Object#equals(Object)} method
     * to decide if there is a difference. Differences are reported using the Result.Strength specified.
     * 
     * @param left
     * @param right
     * @param differences
     * @param strength
     */
    void compareSimple(DbProperty left, DbProperty right, DiffContext ctx, Strength strength);

}