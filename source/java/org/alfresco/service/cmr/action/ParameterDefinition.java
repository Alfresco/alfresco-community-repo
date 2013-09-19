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
import org.alfresco.service.namespace.QName;

/**
 * Parameter definition interface.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ParameterDefinition 
{
	/**
	 * Get the name of the parameter.
	 * <p>
	 * This is unique and is used to identify the parameter.
	 * 
	 * @return	the parameter name
	 */
	public String getName();
	
	/**
	 * Get the type of parameter
	 * 
	 * @return	the parameter type qname
	 */
	public QName getType();
	
    /**
     * Is multi-valued?
     */
    public boolean isMultiValued();
    
	/**
	 * Indicates whether the parameter is mandatory or not.
	 * <p>
	 * If a parameter is mandatory it means that the value can not be null.
	 * 
	 * @return	true if the parameter is mandatory, false otherwise
	 */
	public boolean isMandatory();
	
	/**
	 * Get the display label of the parameter.
	 * 
	 * @return	the parameter display label
	 */
	public String getDisplayLabel();
	
	/**
	 * Gets the parameter constraint name, null if none set.
	 * 
	 * @return   the parameter constraint name
	 */
	public String getParameterConstraintName();
	
}
