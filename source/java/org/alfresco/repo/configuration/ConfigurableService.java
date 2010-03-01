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
package org.alfresco.repo.configuration;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Configurable service interface
 * 
 * @author Roy Wetherall
 */
public interface ConfigurableService 
{
    
    
	/**
	 * Indicates whether a node is configurable or not
	 * 
	 * @param nodeRef	the node reference
	 * @return			true if the node is configurable, false otherwise
	 */
	public boolean isConfigurable(NodeRef nodeRef);
	
	/**
     * Makes a specified node Configurable.
     * <p>
     * This will create the cofigurable folder, associate it as a child of the node and apply the 
     * configurable aspect to the node.
     * 
     * @param nodeRef the node reference
     */
    public void makeConfigurable(NodeRef nodeRef);
    
    /**
     * Get the configuration folder associated with a configuration node
     * 
     * @param nodeRef   the node reference
     * @return			the configuration folder
     */
    public NodeRef getConfigurationFolder(NodeRef nodeRef);

}
