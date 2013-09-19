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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Rule item interface
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ParameterizedItem
{
	/**
	 * Unique identifier for the parameterized item
	 * 
	 * @return	the id string
	 */
	public String getId();
	
	/**
	 * Get the parameter values
	 * 
	 * @return	get the parameter values
	 */
	public Map<String, Serializable> getParameterValues();
	
	/**
	 * Get value of a named parameter.
	 * 
	 * @param name	the parameter name
	 * @return		the value of the parameter
	 */
	public Serializable getParameterValue(String name);
	
	/**
	 * Sets the parameter values
	 * 
	 * @param parameterValues	the parameter values
	 */
	public void setParameterValues(
            Map<String, Serializable> parameterValues);
	
	/**
	 * Sets the value of a parameter.
	 * 
	 * @param name		the parameter name
	 * @param value		the parameter value
	 */
	public void setParameterValue(String name, Serializable value);
}
