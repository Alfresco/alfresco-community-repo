/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.audit.model;

/**
 * An enum for the values 
 * <ol>
 *   <li> TRUE
 *   <li> FALSE
 *   <li> UNSET
 * </ol>
 * 
 * @author Andy Hind
 */
public enum TrueFalseUnset
{
    TRUE, FALSE, UNSET;
    
    public static TrueFalseUnset getTrueFalseUnset(String value)
    {
        if(value.equalsIgnoreCase("true"))
        {
            return TrueFalseUnset.TRUE;
        }
        else if(value.equalsIgnoreCase("false"))
        {
            return TrueFalseUnset.FALSE;
        }
        else if(value.equalsIgnoreCase("unset"))
        {
            return TrueFalseUnset.UNSET;
        }
        else
        {
            throw new AuditModelException("Invalid value for TrueFalseUnset: "+value);
        }
    }
}   
