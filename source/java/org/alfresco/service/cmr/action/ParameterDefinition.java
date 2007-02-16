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

import org.alfresco.service.namespace.QName;

/**
 * Parameter definition interface.
 * 
 * @author Roy Wetherall
 */
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
	
}
