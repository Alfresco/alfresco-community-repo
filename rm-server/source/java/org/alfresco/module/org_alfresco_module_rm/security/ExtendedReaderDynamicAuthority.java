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
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extended readers dynamic authority implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedReaderDynamicAuthority implements DynamicAuthority, 
                                                       RecordsManagementModel, 
                                                       ApplicationContextAware
{
    /** Extended reader role */
    public static final String EXTENDED_READER = "ROLE_EXTENDED_READER";
    
    /** Authority service */
    private AuthorityService authorityService;
    
    /** Records management security service */
    private RecordsManagementSecurityService recordsManagementSecurityService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Application context */
    private ApplicationContext applicationContext;
    
    // NOTE: we get the services directly from the application context in this way to avoid
    //       cyclic relationships and issues when loading the application context
    
    /**
     * @return  authority service
     */
    private AuthorityService getAuthorityService()
    {
        if (authorityService == null)
        {
            authorityService = (AuthorityService)applicationContext.getBean("authorityService");
        }
        return authorityService;
    }
    
    /**
     * @return  records management security service
     */
    public RecordsManagementSecurityService getRecordsManagementSecurityService()
    {
        if (recordsManagementSecurityService == null)
        {
            recordsManagementSecurityService = (RecordsManagementSecurityService)applicationContext.getBean("recordsManagementSecurityService");
        }
        return recordsManagementSecurityService;
    }
    
    /**
     * @return  node service
     */
    public NodeService getNodeService()
    {
        if (nodeService == null)
        {
            nodeService = (NodeService)applicationContext.getBean("nodeService");
        }
        return nodeService;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
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
        return EXTENDED_READER;
    }

    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#hasAuthority(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public boolean hasAuthority(NodeRef nodeRef, String userName)
    {
        boolean result = false;
        
        if (getNodeService().hasAspect(nodeRef, ASPECT_EXTENDED_READERS) == true)
        {
            Set<String> readers = getRecordsManagementSecurityService().getExtendedReaders(nodeRef);
            if (readers != null)
            {
                for (String reader : readers)
                {
                    if ("GROUP_EVERYONE".equals(reader) == true)
                    {
                        // 'eveyone' has read
                        result = true;
                        break;
                    }
                    else if (reader.startsWith("GROUP_") == true)
                    {
                        // check group to see if the user is contained
                        Set<String> contained = getAuthorityService().getContainedAuthorities(AuthorityType.USER, reader, false);
                        if (contained.isEmpty() == false && 
                            contained.contains(userName) == true)
                        {
                            result = true;
                            break;
                        }
                    }
                    else
                    {
                        // presume we have a user
                        if (reader.equals(userName) == true)
                        {
                            result = true;
                            break;
                        }
                    }
                }
            }
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
