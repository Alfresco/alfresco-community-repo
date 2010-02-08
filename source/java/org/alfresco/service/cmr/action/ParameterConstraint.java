/*
 * Copyright (C) 2009-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.Map;

/**
 * Parameter constraint.  Helps to constraint the list of allowable values for a 
 * 
 * @author Roy Wetherall
 */
public interface ParameterConstraint
{
    /**
     * Gets the unique name of the constraint
     * 
     * @return String   constraint name
     */
    String getName();
    
    /**
     * Indicates whether the provided value satisfies the constraint.  True if it does, false otherwise.
     * 
     * @return  boolean  true if valid, false otherwise
     */
    boolean isValidValue(String value);
    
    /**
     * 
     * @param value
     * @return
     */
    String getValueDisplayLabel(String value);
    
    /**
     * 
     */
    Map<String, String> getAllowableValues();
}
