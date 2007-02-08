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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.configuration;

import java.util.List;

import org.alfresco.model.ApplicationModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * @author Roy Wetherall
 */
public class ConfigurableServiceImpl implements ConfigurableService
{
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public boolean isConfigurable(NodeRef nodeRef)
	{
		return this.nodeService.hasAspect(nodeRef, ApplicationModel.ASPECT_CONFIGURABLE);
	}

	public void makeConfigurable(NodeRef nodeRef)
	{
		if (isConfigurable(nodeRef) == false)
		{
			// First apply the aspect
			this.nodeService.addAspect(nodeRef, ApplicationModel.ASPECT_CONFIGURABLE, null);
			
			// Next create and add the configurations folder
			this.nodeService.createNode(
					nodeRef,
					ApplicationModel.ASSOC_CONFIGURATIONS,
					ApplicationModel.ASSOC_CONFIGURATIONS,
					ApplicationModel.TYPE_CONFIGURATIONS);
		}		
	}
	
	public NodeRef getConfigurationFolder(NodeRef nodeRef)
	{
		NodeRef result = null;
		if (isConfigurable(nodeRef) == true)
		{
			List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(
                    nodeRef,
                    RegexQNamePattern.MATCH_ALL,
                    ApplicationModel.ASSOC_CONFIGURATIONS);
			if (assocs.size() != 0)
			{
				ChildAssociationRef assoc = assocs.get(0);
				result = assoc.getChildRef();
			}
		}		
		return result;
	}

}
