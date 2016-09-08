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
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * RM v2.1: RM admin user patch
 * 
 * @author Roy Wetherall
 */
public class RMv2RMAdminUserPatch extends ModulePatchComponent implements BeanNameAware
{
    /** I18N */
    private static final String MSG_FIRST_NAME = "bootstrap.rmadmin.firstName";
    private static final String MSG_LAST_NAME = "bootstrap.rmadmin.lastName";

    /** generate rm admin password */
    private String password = GUID.generate();
    
    /** mutable authenticaiton service */
    private MutableAuthenticationService authenticationService;
    
    /** person service */
    private PersonService personService;
    
    /** file plan service */
    private FilePlanService filePlanService;
    
    /** file plan role service */
    private FilePlanRoleService filePlanRoleService;
    
    /** file plan authentication service */
    private FilePlanAuthenticationService filePlanAuthenticationService;
    
    /**     
     * @param personService person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * @param authenticationService mutable authentication service
     */
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }
    
    /**
     * @param filePlanAuthenticationService file plan authentication service
     */
    public void setFilePlanAuthenticationService(FilePlanAuthenticationService filePlanAuthenticationService)
    {
        this.filePlanAuthenticationService = filePlanAuthenticationService;
    }
    
    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch() throws Throwable
    {
        String user = filePlanAuthenticationService.getRmAdminUserName();
        String firstName = I18NUtil.getMessage(MSG_FIRST_NAME);
        String lastName = I18NUtil.getMessage(MSG_LAST_NAME);

        if (authenticationService.authenticationExists(user) == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("   ... creating RM Admin user");
            }
            
            authenticationService.createAuthentication(user, password.toCharArray());
            
            if (personService.personExists(user) == false)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("   ... creating RM Admin person");
                }
                
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_USERNAME, user);
                properties.put(ContentModel.PROP_FIRSTNAME, firstName);
                properties.put(ContentModel.PROP_LASTNAME, lastName);
                personService.createPerson(properties);
            }
            else
            {
                if (logger.isInfoEnabled() == true)
                {
                    logger.debug("   ... RM Admin person already exists");
                }
            }
                
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("   ... assigning RM Admin user to file plans");
            }
            
            Set<NodeRef> filePlans = filePlanService.getFilePlans();
            for (NodeRef filePlan : filePlans)
            {
                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_ADMIN, user);
            }
        }
    }
}
