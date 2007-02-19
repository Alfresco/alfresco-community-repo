/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
