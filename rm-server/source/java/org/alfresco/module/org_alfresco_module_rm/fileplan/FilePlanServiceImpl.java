/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.fileplan;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedReaderDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedWriterDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * File plan service implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanServiceImpl extends ServiceBaseImpl
                                 implements FilePlanService, 
                                            RecordsManagementModel,
                                            ApplicationContextAware
{
    /** Unfiled record container name */
    private static final String NAME_UNFILED_CONTAINER = "Unfiled Records";
    private static final QName QNAME_UNFILED_CONTAINER = QName.createQName(RM_URI, NAME_UNFILED_CONTAINER);
    
    /** RM site file plan container */
    private static final String FILE_PLAN_CONTAINER = "documentLibrary";
    
    /** Application context */
    private ApplicationContext applicationContext;
    
    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * NOTE:  for some reason spring couldn't cope with the circular references between these two 
     *        beans so we need to grab this one manually.
     * 
     * @return  file plan role service
     */
    protected FilePlanRoleService getFilePlanRoleService()
    {
        return (FilePlanRoleService)applicationContext.getBean("FilePlanRoleService");        
    }
    
    protected PermissionService getPermissionService()
    {
        return (PermissionService)applicationContext.getBean("permissionService"); 
    }
    
    protected NodeDAO getNodeDAO()
    {
        return (NodeDAO)applicationContext.getBean("nodeDAO"); 
    }
    
    protected NodeService getInternalNodeService()
    {
        return (NodeService)applicationContext.getBean("nodeService"); 
    }
    
    protected SiteService getSiteService()
    {
        return (SiteService)applicationContext.getBean("SiteService"); 
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#isFilePlan(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isFilePlan(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_FILE_PLAN);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getFilePlans()
     */
    @Override
    public Set<NodeRef> getFilePlans()
    {
        return getFilePlans(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getFilePlans(org.alfresco.service.cmr.repository.StoreRef)
     */
    @Override
    public Set<NodeRef> getFilePlans(final StoreRef storeRef)
    {
        ParameterCheck.mandatory("storeRef", storeRef);
        
        final Set<NodeRef> results = new HashSet<NodeRef>();
        Set<QName> aspects = new HashSet<QName>(1);
        aspects.add(ASPECT_RECORDS_MANAGEMENT_ROOT);
        getNodeDAO().getNodesWithAspects(aspects, Long.MIN_VALUE, Long.MAX_VALUE, new NodeDAO.NodeRefQueryCallback()
        {            
            @Override
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                NodeRef nodeRef = nodePair.getSecond();
                if (storeRef.equals(nodeRef.getStoreRef()) == true)
                {                
                    results.add(nodeRef);
                }
                
                return true;
            }
        });
        return results;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getFilePlan(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef getFilePlan(NodeRef nodeRef)
    {
        NodeRef result = null;        
        if (nodeRef != null)
        {
             result = (NodeRef)getInternalNodeService().getProperty(nodeRef, PROP_ROOT_NODEREF);
             if (result == null)
             {
                 if (instanceOf(nodeRef, TYPE_FILE_PLAN) == true)
                 {
                     result = nodeRef;
                 }
                 else
                 {
                     ChildAssociationRef parentAssocRef = getInternalNodeService().getPrimaryParent(nodeRef);
                     if (parentAssocRef != null)
                     {
                         result = getFilePlan(parentAssocRef.getParentRef());
                     }
                 }
             }
        }      
         
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getFilePlanBySiteId(java.lang.String)
     */
    @Override
    public NodeRef getFilePlanBySiteId(String siteId)
    {
        NodeRef filePlan = null;    
        
        SiteInfo siteInfo = getSiteService().getSite(siteId);
        if (siteInfo != null)
        {
            if (getSiteService().hasContainer(siteId, FILE_PLAN_CONTAINER) == true)
            {
                NodeRef nodeRef = getSiteService().getContainer(siteId, FILE_PLAN_CONTAINER);
                if (instanceOf(nodeRef, TYPE_FILE_PLAN) == true)
                {
                    filePlan = nodeRef;
                }
            }
        }
        
        return filePlan;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#existsUnfiledContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean existsUnfiledContainer(NodeRef filePlan)
    {
        return (getUnfiledContainer(filePlan) != null);
    }    
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getUnfiledContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef getUnfiledContainer(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        if (isFilePlan(filePlan) == false)
        {
            throw new AlfrescoRuntimeException("Unable to get the unfiled container, because passed node is not a file plan.");
        }

        NodeRef result = null;
             
        // try and get the unfiled record container
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(filePlan, ContentModel.ASSOC_CONTAINS, QNAME_UNFILED_CONTAINER);
        if (assocs.size() > 1)
        {
            throw new AlfrescoRuntimeException("Unable to get unfiled conatiner.");
        }
        else
        {
            result = assocs.get(0).getChildRef();
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createUnfiledContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef createUnfiledContainer(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        if (isFilePlan(filePlan) == false)
        {
            throw new AlfrescoRuntimeException("Unable to create unfiled container, because passed node is not a file plan.");
        }
        
        String allRoles = getFilePlanRoleService().getAllRolesContainerGroup(filePlan);
        
        // create the properties map
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, NAME_UNFILED_CONTAINER);

        // create the unfiled container
        NodeRef container = nodeService.createNode(
                        filePlan,
                        ContentModel.ASSOC_CONTAINS,
                        QNAME_UNFILED_CONTAINER,
                        TYPE_UNFILED_RECORD_CONTAINER,
                        properties).getChildRef();

        // set inheritance to false
        getPermissionService().setInheritParentPermissions(container, false);
        getPermissionService().setPermission(container, allRoles, RMPermissionModel.READ_RECORDS, true);
        getPermissionService().setPermission(container, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
        getPermissionService().setPermission(container, ExtendedWriterDynamicAuthority.EXTENDED_WRITER, RMPermissionModel.FILING, true);
        
        // TODO set the admin users to have filing permissions on the unfiled container!!!
        // TODO we will need to be able to get a list of the admin roles from the service

        return container;
    }
    

}
