/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.Map;

/**
 * Rule item interface
 * 
 * @author Roy Wetherall
 */
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
