/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Parameter constraint.  Helps to constraint the list of allowable values for a 
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
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
