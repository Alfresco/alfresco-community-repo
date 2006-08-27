/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
