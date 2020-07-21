/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
     */
    public List<Column> getColumns();
    
    /**
     * Get the constraints for the query.
     * This is as defined - with no hoisting etc.
     * Hoisting is the problem of the implementation layer.
     * 
     * May be null for unconstrained.
     * 
     * @return Constraint
     */
    public Constraint getConstraint();
    
    /**
     * Get any orderings (may be an empty list or null)
     *  
     */
    public List<Ordering> getOrderings();
    
    /**
     * Get the source for the query
     * Must not be null.
     * @return Source
     */
    public Source getSource();
}
