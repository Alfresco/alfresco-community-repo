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
