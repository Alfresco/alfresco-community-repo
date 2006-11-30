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
