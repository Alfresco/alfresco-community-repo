/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.service.cmr.action;

import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Parameter constraint.  Helps to constraint the list of allowable values for a action parameter.
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
     * @param value String
     * @return String
     */
    String getValueDisplayLabel(String value);
    
    /**
     *  The implementers are expected to return allowed values in the insertion order.
     */
    Map<String, String> getAllowableValues();
}
