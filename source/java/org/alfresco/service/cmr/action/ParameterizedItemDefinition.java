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

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;

@AlfrescoPublicApi
public interface ParameterizedItemDefinition 
{
	/**
	 * Get the name of the rule item.
	 * <p>
	 * The name is unique and is used to identify the rule item.
	 * 
	 * @return	the name of the rule action
	 */
	public String getName();
	
	/**
	 * The title of the parameterized item definition
	 * 
	 * @return	the title
	 */
	public String getTitle();
	
	/**
	 * The description of the parameterized item definition
	 * 
	 * @return	the description
	 */
	public String getDescription();
	
	/**
	 * Indicates whether the parameterized item allows adhoc properties to be set
	 * 
	 * @return	true if ashoc properties are allowed, false otherwise
	 */
	public boolean getAdhocPropertiesAllowed();
    
    /**
     * Indicates whether the parameterized item has any parameter definitions
     * 
     * @return` true if the parameterized item has any parameter definitions, false otherwise
     */
    public boolean hasParameterDefinitions();
	
	/**
	 * A list containing the parmameter defintions for this rule item.
	 * 
	 * @return	a list of parameter definitions
	 */
	public List<ParameterDefinition> getParameterDefinitions();
    
    /**
     * Get the parameter definition by name
     * 
     * @param name  the name of the parameter
     * @return      the parameter definition, null if none found
     */
    public ParameterDefinition getParameterDefintion(String name);
}
