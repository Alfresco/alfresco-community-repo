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

import org.alfresco.util.schemacomp.Differences;

/**
 * All database objects to be modelled for schema comparisons must implement this interface, examples
 * of which include: schemas, tables, indexes, primary keys, foreign keys, sequences and columns.
 * 
 * @author Matt Ward
 */
public interface DbObject
{
    /**
     * Are the two <code>DbObject</code>s logically the same? For example two Index objects may have
     * different names, but are the same index as they both index the same columns for the same table.
     *  
     * @param other
     * @return
     */
    boolean sameAs(DbObject other);
    
    /**
     * All items can be asked for their name, but it may be null.
     * 
     * @return Name if available, null otherwise.
     */
    String getName();
    
    /**
     * Generate a report of differences between this object ('left') and another object ('right').
     * Differences between the left and right objects under inspection are captured in the {@link Differences}
     * object passed in to this method.
     * 
     * @param right The object to compare against.
     * @param differences A collector of differences.
     */
    void diff(DbObject right, Differences differences);
}
