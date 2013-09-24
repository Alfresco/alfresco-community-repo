/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.model.behaviour;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 * Behaviour associated with the RM Site type
 * 
 * @author Roy Wetherall
 */
public class RmSiteType implements RecordsManagementModel,
                                   NodeServicePolicies.OnCreateNodePolicy,
                                   NodeServicePolicies.OnUpdatePropertiesPolicy
{
	/** Constant values */
	public static final String COMPONENT_DOCUMENT_LIBRARY = "documentLibrary";
    public static final String DEFAULT_SITE_NAME = "rm";
	
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Site service */
    private SiteService siteService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Record Management Search Service */
    private RecordsManagementSearchService recordsManagementSearchService;
    
    /** Behaviour */
    JavaBehaviour onCreateNode = new JavaBehaviour(this, "onCreateNode", NotificationFrequency.FIRST_EVENT);
    JavaBehaviour onUpdateProperties = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.FIRST_EVENT);
    
    /**
     * Set the policy component
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the site service
     * @param siteService	site service
     */
    public void setSiteService(SiteService siteService) 
    {
		this.siteService = siteService;
	}
    
    /**
     * Set node service
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param recordsManagementSearchService    records management search service
     */
    public void setRecordsManagementSearchService(RecordsManagementSearchService recordsManagementSearchService)
    {
        this.recordsManagementSearchService = recordsManagementSearchService;
    }
    
    /**
     * Bean initialisation method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
                                           TYPE_RM_SITE,
                                           onCreateNode);
        
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, 
                                           TYPE_RM_SITE, 
                                           onUpdateProperties);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) 
	{
	    onCreateNode.disable();
	    try
	    {	    
    		final NodeRef rmSite = childAssocRef.getChildRef();
            
            // Do not execute behaviour if this has been created in the archive store
            if(rmSite.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE) == true)
            {
                // This is not the spaces store - probably the archive store
                return;
            }
            
            if (nodeService.exists(rmSite) == true)
            {
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                    	SiteInfo siteInfo = siteService.getSite(rmSite);
                    	if (siteInfo != null)
                    	{	                
    	                	// Create the file plan component
    	                	siteService.createContainer(siteInfo.getShortName(), COMPONENT_DOCUMENT_LIBRARY, TYPE_FILE_PLAN, null);
    	                	
    	                	// Add the reports
    	                	recordsManagementSearchService.addReports(siteInfo.getShortName());
                    	}
                        return null;
                    }
                }, AuthenticationUtil.getAdminUserName());
            }
	    }
	    finally
	    {
	        onCreateNode.enable();
	    }
	}

	/**
	 * Ensure that the visibility of a RM site can not be changed to anything but public.
	 * 
	 * TODO support other site visibilities
	 * 
	 * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
	 */
    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef) == true)
        {
            Map<QName, Serializable> changed = PropertyMap.getChangedProperties(before, after);   
            if (changed.containsKey(SiteModel.PROP_SITE_VISIBILITY) == true &&
                changed.get(SiteModel.PROP_SITE_VISIBILITY) != null &&
                SiteVisibility.PUBLIC.equals(changed.get(SiteModel.PROP_SITE_VISIBILITY)) == false)                
            {
                // we do not current support non-public RM sites
                throw new AlfrescoRuntimeException("The records management site must have public visibility.  It can't be changed to " + changed.get(SiteModel.PROP_SITE_VISIBILITY));
            }
        }
    }
}
