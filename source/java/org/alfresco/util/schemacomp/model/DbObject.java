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

import java.util.List;

import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Result.Strength;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.validator.DbValidator;

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
     * <p>
     * If two objects a and b have the same logical identity, it does not mean that <code>a.equals(b) == true</code>.
     * The two objects may well have differences and will be flagged as such by the schema comparison tool. When
     * <code>a.sameAs(b) == true</code> it makes it easier to show the differences as related, i.e. a and b are
     * different rather than, a is only in the 'left' tree and b is only in the 'right' tree. 
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
     * Differences between the left and right objects under inspection are captured in the {@link Results}
     * object passed in to this method.
     * 
     * @param right The object to compare against.
     * @param ctx The DiffContext
     */
    void diff(DbObject right, DiffContext ctx, Strength strength);
    
    
    /**
     * Allows a visitor to be invoked against this DbObject. Implementations should ensure that child
     * objects are visited first (by calling accept on them) before invoking the visitor on itself.
     * 
     * @param visitor
     */
    void accept(DbObjectVisitor visitor);
    
    /**
     * Get the parent object for which this object is a child. If this is the root object
     * then null should be returned.
     * 
     * @return Parent reference or null
     */
    DbObject getParent();
    
    /**
     * Sets the parent object.
     * 
     * @see #getParent()
     * @param parent
     */
    void setParent(DbObject parent);
    
    
    /**
     * Retrieve the list of validators associated with this database object.
     * 
     * @see DbValidator
     * @return DbValidator List
     */
    List<DbValidator> getValidators();
    
    /**
     * Set/override the validators associated with this database object.
     * 
     * @param validators
     */
    void setValidators(List<DbValidator> validators);
}
