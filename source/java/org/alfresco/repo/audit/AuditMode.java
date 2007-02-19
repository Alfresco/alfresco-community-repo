/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
    ALL, SUCCESS, FAIL, NONE, UNSET;
    
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
