package org.alfresco.rest.api.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.opencmis.dictionary.QNameFilter;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Stores a set of excluded types, by full type name. The localName can be a wildcard (*) to indicate that the
 * whole namespace should be excluded.
 * 
 * @author steveglover
 *
 */
public class ExcludedTypes
{
	private List<QName> expectedTypes;
    private QNameFilter excludedTypes;
    private NodeService nodeService;

    public void setExpectedTypes(List<String> expectedTypes)
    {
        if(expectedTypes != null && expectedTypes.size() > 0)
        {
        	this.expectedTypes = new ArrayList<QName>(expectedTypes.size());

	    	for(String type : expectedTypes)
	    	{
	            final QName typeDef = QName.createQName(type);
	    		this.expectedTypes.add(typeDef);
	    	}
        }
	}

	public void setNodeService(NodeService nodeService)
    {
		this.nodeService = nodeService;
	}

	public void setExcludedTypes(QNameFilter excludedTypes)
    {
		this.excludedTypes = excludedTypes;
	}

	public boolean isExcluded(final NodeRef nodeRef)
	{
		boolean excluded = false;

		QName nodeType = nodeService.getType(nodeRef);
		if(expectedTypes != null && !expectedTypes.contains(nodeType))
		{
			excluded = true;
		}

		if(!excluded)
		{
			// need to run as system - caller may not be able to read the node's aspects
			// but we need to know what they are in order to determine exclusion.
			excluded = TenantUtil.runAsSystemTenant(new TenantRunAsWork<Boolean>()
			{
				@Override
				public Boolean doWork() throws Exception
				{
					boolean excluded = false;

					// the node is a content node. Make sure it doesn't have an aspect in the excluded list.
					Set<QName> aspects = new HashSet<QName>(nodeService.getAspects(nodeRef));
					for(QName aspect : aspects)
					{
						if(excludedTypes.isExcluded(aspect))
						{
							excluded = true;
							break;
						}
					}

					return excluded;
				}
			}, TenantUtil.getCurrentDomain());
		}

		return excluded;
	}
}
