/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extended readers dynamic authority implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class ExtendedSecurityBaseDynamicAuthority implements DynamicAuthority,
                                                                      RecordsManagementModel,
                                                                      ApplicationContextAware
{
    /** transaction cache key */
    private static final String KEY_HAS_AUTHORITY_CACHE = "rm.transaction.hasAuthority";
    
    /** Authority service */
    private AuthorityService authorityService;

    /** Extended security service */
    private ExtendedSecurityService extendedSecurityService;

    /** Node service */
    private NodeService nodeService;

    /** Application context */
    protected ApplicationContext applicationContext;

    // NOTE: we get the services directly from the application context in this way to avoid
    //       cyclic relationships and issues when loading the application context

    /**
     * @return  authority service
     */
    protected AuthorityService getAuthorityService()
    {
        if (authorityService == null)
        {
            authorityService = (AuthorityService)applicationContext.getBean("authorityService");
        }
        return authorityService;
    }

    /**
     * @return  extended security service
     */
    protected ExtendedSecurityService getExtendedSecurityService()
    {
        if (extendedSecurityService == null)
        {
            extendedSecurityService = (ExtendedSecurityService)applicationContext.getBean("extendedSecurityService");
        }
        return extendedSecurityService;
    }

    /**
     * @return  node service
     */
    protected NodeService getNodeService()
    {
        if (nodeService == null)
        {
            nodeService = (NodeService)applicationContext.getBean("dbNodeService");
        }
        return nodeService;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Gets a list of the authorities from the extended security aspect that this dynamic
     * authority is checking against.
     *
     * @param nodeRef
     * @return
     */
    protected abstract Set<String> getAuthorites(NodeRef nodeRef);

    /**
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#hasAuthority(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public boolean hasAuthority(NodeRef nodeRef, String userName)
    {
        boolean result = false;

        if (getNodeService().hasAspect(nodeRef, ASPECT_EXTENDED_SECURITY))
        {
            Set<String> authorities = getAuthorites(nodeRef);
            if (authorities != null)
            {
                for (String authority : authorities)
                {
                    if ("GROUP_EVERYONE".equals(authority))
                    {
                        // 'eveyone' is there so break
                        result = true;
                        break;
                    }
                    else if (authority.startsWith("GROUP_"))
                    {
                        Map<String, Boolean> transactionCache = TransactionalResourceHelper.getMap(KEY_HAS_AUTHORITY_CACHE);
                        String key = authority + "|" + userName;
                        if (transactionCache.containsKey(key))
                        {
                            result = transactionCache.get(key);
                            break;
                        }
                        else
                        {
                            Set<String> contained = getAuthorityService().getAuthoritiesForUser(userName);
                            if (contained.contains(authority))
                            {
                                result = true;
                                transactionCache.put(key, result);
                                break;
                            }
                        }
                    }
                    else
                    {
                        // presume we have a user
                        if (authority.equals(userName))
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
     * Base implementation
     *
     * @see org.alfresco.repo.security.permissions.DynamicAuthority#requiredFor()
     */
    @Override
    public Set<PermissionReference> requiredFor()
    {
        return null;
    }
}
