/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;

/**
 * Behaviour associated with the RM Site type
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
        defaultType = "rma:rmsite"
)
public class RmSiteType extends    BaseBehaviourBean
                        implements NodeServicePolicies.OnCreateNodePolicy,
                                   NodeServicePolicies.OnUpdatePropertiesPolicy,
                                   NodeServicePolicies.BeforeDeleteNodePolicy
{
	/** Constant values */
	public static final String COMPONENT_DOCUMENT_LIBRARY = "documentLibrary";
    public static final String DEFAULT_SITE_NAME = "rm";
    public static final QName DEFAULT_FILE_PLAN_TYPE = TYPE_FILE_PLAN;
	
    /** Policy component */
    protected PolicyComponent policyComponent;
    
    /** Site service */
    protected SiteService siteService;
    
    /** Record Management Search Service */
    protected RecordsManagementSearchService recordsManagementSearchService;
    
    /** Capability service */
    protected CapabilityService capabilityService;
    
    /** Map of file plan type's key'ed by corresponding site types */
    protected Map<QName, QName> mapFilePlanType = new HashMap<QName, QName>(3);
    
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
     * @param recordsManagementSearchService    records management search service
     */
    public void setRecordsManagementSearchService(RecordsManagementSearchService recordsManagementSearchService)
    {
        this.recordsManagementSearchService = recordsManagementSearchService;
    }
    
    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * Registers a file plan type for a specific site type.
     * 
     * @param siteType		siteType		sub-type of rma:rmsite
     * @param filePlanType  filePlanType	sub-type of rma:filePlan
     * @since 2.2
     */
    public void registerFilePlanType(QName siteType, QName filePlanType)
    {
    	ParameterCheck.mandatory("siteType", siteType);
    	ParameterCheck.mandatory("filePlanType", filePlanType);
    	
    	// check that the registered site type is a subtype of rma:rmsite
    	if (dictionaryService.isSubClass(siteType, TYPE_RM_SITE) == false)
    	{
    		throw new AlfrescoRuntimeException(
    				"Can't register site type, because site type is not a sub type of rma:rmsite (siteType=" + siteType.toString() + ")");
    	}
    	
    	// check that the registered file plan type is a sub type of rma:filePlan
    	if (dictionaryService.isSubClass(filePlanType, TYPE_FILE_PLAN) == false)
    	{
    		throw new AlfrescoRuntimeException(
    				"Can't register file plan type, because site type is not a sub type of rma:filePlan (filePlanType=" + filePlanType.toString() + ")");
    	}
    	
    	// add site and file plan types to map
    	mapFilePlanType.put(siteType, filePlanType);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
	@Override
	@Behaviour
	(
	        kind = BehaviourKind.CLASS,
	        notificationFrequency = NotificationFrequency.FIRST_EVENT
	)
	public void onCreateNode(ChildAssociationRef childAssocRef) 
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
	                	siteService.createContainer(siteInfo.getShortName(), COMPONENT_DOCUMENT_LIBRARY, getFilePlanType(siteInfo), null);
	                	
	                	// Add the reports
	                	recordsManagementSearchService.addReports(siteInfo.getShortName());
                	}
                    return null;
                }
            }, AuthenticationUtil.getAdminUserName());
        }
	}
	
	/**
	 * Get the file plan type for the given site.
	 * 
	 * @param siteInfo	site info
	 * @return QName	file plan type to create as a container
	 * @since 2.2
	 */
	private QName getFilePlanType(SiteInfo siteInfo)
	{
		ParameterCheck.mandatory("siteInfo", siteInfo);
		
		// set default file plan
		QName result = DEFAULT_FILE_PLAN_TYPE;
		
		// check to see if there is an 'override' for the file plan type given the site type
		QName siteType = nodeService.getType(siteInfo.getNodeRef());
		if (mapFilePlanType.containsKey(siteType) == true)
		{
			result = mapFilePlanType.get(siteType);
		}
		
		return result;
	}

	/**
	 * Ensure that the visibility of a RM site can not be changed to anything but public.
	 * 
	 * TODO support other site visibilities
	 * 
	 * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
	 */
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
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

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        final SiteInfo siteInfo = siteService.getSite(nodeRef);
        if (siteInfo != null)
        {
            // grab the file plan for the RM site
            NodeRef filePlan = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() 
            {
                @Override
                public NodeRef doWork() throws Exception
                {
                    return siteService.getContainer(siteInfo.getShortName(), COMPONENT_DOCUMENT_LIBRARY);
                }
                
            });
            
            if (filePlan != null)
            {
                // determine whether the current user has delete capability on the file plan node
                AccessStatus accessStatus = capabilityService.getCapabilityAccessState(filePlan, "Delete");
                if (AccessStatus.DENIED.equals(accessStatus) == true)
                {
                    throw new AlfrescoRuntimeException("The records management site can not be deleted, because the user doesn't have sufficient privillages to delete the file plan.");
                }
            }
        }
    }
}
