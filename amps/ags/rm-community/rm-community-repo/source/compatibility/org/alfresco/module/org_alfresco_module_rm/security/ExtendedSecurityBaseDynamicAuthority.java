/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.util.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extended readers dynamic authority implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@Deprecated
public abstract class ExtendedSecurityBaseDynamicAuthority implements DynamicAuthority,
                                                                      RecordsManagementModel,
                                                                      ApplicationContextAware
{
    /** Authority service */
    private AuthorityService authorityService;

    /** Extended security service */
    private ExtendedSecurityService extendedSecurityService;

    /** Node service */
    private NodeService nodeService;

    /** Application context */
    protected ApplicationContext applicationContext;

    /** model DAO */
    protected ModelDAO modelDAO;

    /** permission reference */
    protected Set<PermissionReference> requiredFor;

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
     * @return	model DAO
     */
    protected ModelDAO getModelDAO()
    {
    	if (modelDAO == null)
    	{
    		modelDAO = (ModelDAO)applicationContext.getBean("permissionsModelDAO");
    	}
    	return modelDAO;
    }

    /**
     * @return	String transaction cache name
     */
    protected abstract String getTransactionCacheName();

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

        Map<Pair<NodeRef, String>, Boolean> transactionCache = TransactionalResourceHelper.getMap(getTransactionCacheName());
        Pair<NodeRef, String> key = new Pair<>(nodeRef, userName);

        if (transactionCache.containsKey(key))
        {
            result = transactionCache.get(key);
        }
        else
        {
	        if (getNodeService().hasAspect(nodeRef, ASPECT_EXTENDED_SECURITY))
	        {
	            Set<String> authorities = getAuthorites(nodeRef);
	            if (authorities != null)
	            {
	            	// check for everyone or the user
	            	if (authorities.contains("GROUP_EVEYONE") ||
	            		authorities.contains(userName))
	            	{
	            		result = true;
	            	}
	            	else
	            	{
	            		// determine whether any of the users groups are in the extended security
	            		Set<String> contained = getAuthorityService().getAuthoritiesForUser(userName);
	            		authorities.retainAll(contained);
	            		result = (authorities.size() != 0);
	            	}
	            }
	        }

	        // cache result
	        transactionCache.put(key, result);
        }

        return result;
    }
}
