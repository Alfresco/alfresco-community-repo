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
package org.alfresco.repo.audit;

import org.alfresco.repo.audit.model.AuditModelException;

/**
 * An enum to specify the audit mode:
 * 
 * <ol>
 *   <li> ALL - all calls are audited
 *   <li> SUCCESS - only successful calls are audited (audited in the same TX)
 *   <li> FAIL - only fail calls are audited (in a new transaction)
 *   <li> NONE - noting is audited 
 *   <li> UNSET
 * </ol>
 * 
 * The mode is inherited from containers if nothing is specified
 * 
 * @author Andy Hind
 */
public enum AuditMode
{
    /**
     * All 
     */
    ALL, 
    
    /**
     * Only success
     */
    SUCCESS, 
     
     /**
      * Only exceptions/failures
      */
    FAIL, 
    
    /**
     * Nothing
     */
    NONE,
    
    /**
     * Don't know
     */
    UNSET;
    
    /**
     * Parse the audit mode from a string
     * 
     * @param value
     * @return - the audit mode.
     */
    public static AuditMode getAuditMode(String value)
    {
        if(value.equalsIgnoreCase("all"))
        {
            return AuditMode.ALL;
        }
        else if(value.equalsIgnoreCase("success"))
        {
            return AuditMode.SUCCESS;
        }
        else if(value.equalsIgnoreCase("fail"))
        {
            return AuditMode.FAIL;
        }
        else if(value.equalsIgnoreCase("none"))
        {
            return AuditMode.NONE;
        }
        else if(value.equalsIgnoreCase("unset"))
        {
            return AuditMode.UNSET;
        }
        else
        {
            throw new AuditModelException("Invalid audit mode: "+value);
        }
    }
}
