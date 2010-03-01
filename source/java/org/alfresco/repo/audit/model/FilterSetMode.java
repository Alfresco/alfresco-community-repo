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
package org.alfresco.repo.audit.model;

/**
 * The enum to define if elements of a filter set are combined using AND or OR.
 * 
 * @author Andy Hind
 */
public enum FilterSetMode
{
    AND, OR;
    
    public static FilterSetMode getFilterSetMode(String value)
    {
        if(value.equalsIgnoreCase("or"))
        {
            return FilterSetMode.OR;
        }
        else if(value.equalsIgnoreCase("or"))
        {
            return FilterSetMode.AND;
        }
        else
        {
            throw new AuditModelException("Invalid FilterSetMode: "+value);
        }
    }
}
