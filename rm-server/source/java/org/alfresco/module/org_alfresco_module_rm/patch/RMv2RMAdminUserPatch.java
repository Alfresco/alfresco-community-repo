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
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationServiceImpl;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.1: RM admin user patch
 * 
 * @author Roy Wetherall
 */
public class RMv2RMAdminUserPatch extends AbstractModuleComponent implements BeanNameAware
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RMv2RMAdminUserPatch.class);

    private String password = FilePlanAuthenticationServiceImpl.DEFAULT_RM_ADMIN_PWD;
    
    private MutableAuthenticationService authenticationService;
    
    private PersonService personService;
    
    private RecordsManagementService recordsManagementService;
    
    private FilePlanRoleService filePlanRoleService;
    
    private FilePlanAuthenticationService filePlanAuthenticationService;
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }

    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }
    
    public void setFilePlanAuthenticationService(FilePlanAuthenticationService filePlanAuthenticationService)
    {
        this.filePlanAuthenticationService = filePlanAuthenticationService;
    }
    
    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal() throws Throwable
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("RM Module RMv2RMAdminUserPatch ...");
        }                
        
        String user = filePlanAuthenticationService.getRmAdminUserName();
        if (authenticationService.authenticationExists(user) == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("   ... creating RM Admin user");
            }
            
            authenticationService.createAuthentication(user, password.toCharArray());
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
            properties.put(ContentModel.PROP_USERNAME, user);
            personService.createPerson(properties);
            
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("   ... assigning RM Admin user to file plans");
            }
            
            List<NodeRef> filePlans = recordsManagementService.getFilePlans();
            for (NodeRef filePlan : filePlans)
            {
                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_ADMIN, user);
            }
            
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("   ... RMv2RMAdminUserPatch complete");
            }
        }

    }
}
