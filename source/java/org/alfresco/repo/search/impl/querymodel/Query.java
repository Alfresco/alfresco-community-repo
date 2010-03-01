/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.querymodel;

import java.util.List;

/**
 * @author andyh
 *
 */
public interface Query
{
    /**
     * Get the columns to return from the query
     * 
     * This may not be null and must contain at least one entry.
     * "*"  "A.*" etc column specifications are not supported.
     * These should have been previously expanded between any query parse and building the query model. 
     * 
     * @return
     */
    public List<Column> getColumns();
    
    /**
     * Get the constraints for the query.
     * This is as defined - with no hoisting etc.
     * Hoisting is the problem of the implementation layer.
     * 
     * May be null for unconstrained.
     * 
     * @return
     */
    public Constraint getConstraint();
    
    /**
     * Get any orderings (may be an empty list or null)
     *  
     * @return
     */
    public List<Ordering> getOrderings();
    
    /**
     * Get the source for the query
     * Must not be null.
     * @return
     */
    public Source getSource();
}
