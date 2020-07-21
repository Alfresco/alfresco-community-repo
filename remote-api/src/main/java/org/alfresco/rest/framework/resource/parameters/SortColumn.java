/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.resource.parameters;

/**
 * Search sort column 
 */
public class SortColumn
{
    public static final String ASCENDING = "ASC";
    public static final String DESCENDING = "DESC";
    
    /**
     * Constructor
     * 
     * @param column  column to sort on
     * @param asc  sort direction
     */
    public SortColumn(String column, boolean asc)
    {
        this.column = column;
        this.asc = asc;
    }
    
    public String column;
    public boolean asc;
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SortColumn [column=");
        builder.append(this.column);
        builder.append(", asc=");
        builder.append(this.asc);
        builder.append("]");
        return builder.toString();
    }
}