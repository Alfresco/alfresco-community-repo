/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
