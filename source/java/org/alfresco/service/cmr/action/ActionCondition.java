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
package org.alfresco.service.cmr.action;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Rule condition interface
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ActionCondition extends ParameterizedItem
{
	/**
	 * Get the action condition definition name
	 * 
	 * @param	the action condition definition name
	 */
    public String getActionConditionDefinitionName();
    
    /**
     * Set whether the condition result should be inverted.
     * <p>
     * This is achieved by applying the NOT logical operator to the
     * result.
     * <p>
     * The default value is false.
     * 
     * @param invertCondition   true indicates that the result of the condition
     *                          is inverted, false otherwise.
     */
    public void setInvertCondition(boolean invertCondition);
    
    /**
     * Indicates whether the condition result should be inverted.
     * <p>
     * This is achieved by applying the NOT logical operator to the result.
     * <p>
     * The default value is false.
     * 
     * @return  true indicates that the result of the condition is inverted, false 
     *          otherwise
     */
    public boolean getInvertCondition();
}
