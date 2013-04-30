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

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedReaderDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedWriterDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.repository.NodeRef;
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
    /** Extended reader and writer role details */
    private static final String ROLE_READERS = "ExtendedReaders";
    private static final String ROLE_READERS_LABEL = "In-Place Readers";
    private static final String[] ROLE_READERS_CAPABILITIES = new String[]
    {
       "ViewRecords"
    };
    private static final String ROLE_WRITERS = "ExtendedWriters";
    private static final String ROLE_WRITERS_LABEL = "In-Place Writers";
    private static final String[] ROLE_WRITERS_CAPABILITIES = new String[]
    {
       "ViewRecords",
       "EditNonRecordMetadata"
    };
    
    /** Logger */
    private static Log logger = LogFactory.getLog(RMv21InPlacePatch.class);  
    
    /** file plan role service */
    private FilePlanRoleService filePlanRoleService;
    
    /** file plan service */
    private FilePlanService filePlanService;
    
    /** File plan permission service */
    private FilePlanPermissionService filePlanPermissionService;
    
    /** capability service */
    private CapabilityService capabilityService;
    
    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }
    
    /**
     * @param filePlanPermissionService file plan permission service
     */
    public void setFilePlanPermissionService(FilePlanPermissionService filePlanPermissionService)
    {
        this.filePlanPermissionService = filePlanPermissionService;
    }
    
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
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
        
        Set<NodeRef> filePlans = filePlanService.getFilePlans();
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("  ... updating " + filePlans.size() + " file plans");
        }
        
        for (NodeRef filePlan : filePlans)
        {
            if (filePlanService.getUnfiledContainer(filePlan) == null)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("  ... updating file plan " + filePlan.toString());
                }
                
                // set permissions
                filePlanPermissionService.setPermission(filePlan, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS);
                filePlanPermissionService.setPermission(filePlan, ExtendedWriterDynamicAuthority.EXTENDED_WRITER, RMPermissionModel.FILING);
                            
                // create unfiled container
                filePlanService.createUnfiledContainer(filePlan);            
                
                // add the inplace roles
                filePlanRoleService.createRole(filePlan, ROLE_READERS, ROLE_READERS_LABEL, getCapabilities(ROLE_READERS_CAPABILITIES));
                filePlanRoleService.createRole(filePlan, ROLE_WRITERS, ROLE_WRITERS_LABEL, getCapabilities(ROLE_WRITERS_CAPABILITIES));
            }
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug(" ... complete");
        }
    }   
    
    private Set<Capability> getCapabilities(String[] capabilityNames)
    {
        Set<Capability> capabilities = new HashSet<Capability>(3);
        for (String capabilityName : capabilityNames)
        {
            capabilities.add(capabilityService.getCapability(capabilityName));
        }
        return capabilities;
    }
}
