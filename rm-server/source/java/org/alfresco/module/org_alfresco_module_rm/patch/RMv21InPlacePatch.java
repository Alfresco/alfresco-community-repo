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
package org.alfresco.module.org_alfresco_module_rm.patch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedReaderDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityServiceImpl;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.1 patch to support InPlace functional updates
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RMv21InPlacePatch extends AbstractModuleComponent 
                               implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RMv21InPlacePatch.class);  
    
    /** Node service */
    private NodeService nodeService;
    
    /** Permission service */
    private PermissionService permissionService;
    
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    /** Records management security service */
    private RecordsManagementSecurityService recordsManagementSecurityService;
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    /**
     * @param recordsManagementSecurityService  records management security service
     */
    public void setRecordsManagementSecurityService(RecordsManagementSecurityService recordsManagementSecurityService)
    {
        this.recordsManagementSecurityService = recordsManagementSecurityService;
    }
    
    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal() throws Throwable
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("RM module: RMv21InPlacePatch executing ...");
        }
        
        List<NodeRef> filePlans = recordsManagementService.getFilePlans();
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("  ... updating " + filePlans.size() + " file plans");
        }
        
        for (NodeRef filePlan : filePlans)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("  ... updating file plan " + filePlan.toString());
            }
            
            // set permissions
            recordsManagementSecurityService.setPermission(filePlan, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS);
            permissionService.setPermission(filePlan, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.VIEW_RECORDS, true);
            
            // create unfiled container
            createUnfiledContainer(filePlan);            
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug(" ... complete");
        }
    }   
    
    /**
     * TODO .. this should really be moved to a service call ... should be on the FilPlanService?
     */
    private NodeRef createUnfiledContainer(NodeRef filePlan)
    {
        String allRoles = recordsManagementSecurityService.getAllRolesContainerGroup(filePlan);
        
        // create the properties map
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, FilePlanRoleServiceImpl.NAME_UNFILED_CONTAINER);

        // create the unfiled container
        NodeRef container = nodeService.createNode(
                        filePlan,
                        ASSOC_UNFILED_RECORDS,
                        QName.createQName(RM_URI, FilePlanRoleServiceImpl.NAME_UNFILED_CONTAINER),
                        TYPE_UNFILED_RECORD_CONTAINER,
                        properties).getChildRef();

        // set inheritance to false
        permissionService.setInheritParentPermissions(container, false);
        permissionService.setPermission(container, allRoles, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(container, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
        
        // TODO set the admin users to have filing permissions on the unfiled container!!!
        // TODO we will need to be able to get a list of the admin roles from the service

        return container;
    }
    
}
