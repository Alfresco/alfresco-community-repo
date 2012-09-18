/*
`        * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.permission;

import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordReadersDynamicAuthority implements DynamicAuthority, RecordsManagementModel, ApplicationContextAware
{
    public static final String RECORD_READERS = "ROLE_RECORD_READERS";
    
    private RecordsManagementService recordsManagementService;
    
    private NodeService nodeService;
    
    private AuthorityService authorityService;
    
    private ApplicationContext applicationContext;
    
    private RecordsManagementService getRecordsManagementService()
    {
        if (recordsManagementService == null)
        {
            recordsManagementService = (RecordsManagementService)applicationContext.getBean("recordsManagementService");
        }
        return recordsManagementService;
    }
    
    private NodeService getNodeService()
    {
        if (nodeService == null)
        {
            nodeService = (NodeService)applicationContext.getBean("nodeService");
        }
        return nodeService;
    }
    
    private AuthorityService getAuthorityService()
    {
        if (authorityService == null)
        {
            authorityService = (AuthorityService)applicationContext.getBean("authorityService");
        }
        return authorityService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#getAuthority()
     */
    @Override
    public String getAuthority()
    {
        return RECORD_READERS;
    }

    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#hasAuthority(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean hasAuthority(NodeRef nodeRef, String userName)
    {
        boolean result = false;
        
        FilePlanComponentKind kind = getRecordsManagementService().getFilePlanComponentKind(nodeRef);
        if (FilePlanComponentKind.RECORD.equals(kind) == true)
        {
            if (getNodeService().hasAspect(nodeRef, ASPECT_EXTENDED_RECORD_SECURITY) == true)
            {
                List<String> readers = (List<String>)nodeService.getProperty(nodeRef, PROP_READERS);
                for (String reader : readers)
                {
                    if (reader.startsWith("GROUP_") == true)
                    {
                        Set<String> contained = getAuthorityService().getContainedAuthorities(AuthorityType.USER, reader, false);
                        if (contained.isEmpty() == false && 
                            contained.contains(userName) == true)
                        {
                            System.out.println("User " + userName + " is contained in the read group " + reader);
                            
                            result = true;
                            break;
                        }
                    }
                    else
                    {
                        // presume we have a user
                        if (reader.equals(userName) == true)
                        {
                            System.out.println("User " + userName + " matches read user " + reader);
                            
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        else if (FilePlanComponentKind.FILE_PLAN.equals(kind) == true)
        {
            result = true;
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#requiredFor()
     */
    @Override
    public Set<PermissionReference> requiredFor()
    {
        return null;
    }
}
