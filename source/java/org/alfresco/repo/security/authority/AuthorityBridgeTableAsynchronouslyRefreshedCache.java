/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.authority;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.AbstractMTAsynchronouslyRefreshedCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.BridgeTable;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Andy
 * @since 4.1.3
 */
public class AuthorityBridgeTableAsynchronouslyRefreshedCache extends  AbstractMTAsynchronouslyRefreshedCache<BridgeTable<String>> implements InitializingBean
{
    private AuthorityBridgeDAO authorityBridgeDAO;
    private RetryingTransactionHelper retryingTransactionHelper;
    private TenantAdminService tenantAdminService;
    private AuthorityDAO authorityDAO;

    private Log logger = LogFactory.getLog(getClass());

    /**
     * @param authorityDAO
     *            the authorityDAO to set
     */
    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }

    /**
     * @param authorityBridgeDAO
     *            the authorityBridgeDAO to set
     */
    public void setAuthorityBridgeDAO(AuthorityBridgeDAO authorityBridgeDAO)
    {
        this.authorityBridgeDAO = authorityBridgeDAO;
    }

    /**
     * @param retryingTransactionHelper
     *            the retryingTransactionHelper to set
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    @Override
    protected BridgeTable<String> buildCache(final String tenantId)
    {
        return AuthenticationUtil.runAs(new RunAsWork<BridgeTable<String>>()
        {
            public BridgeTable<String> doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<BridgeTable<String>>()
                {
                    @Override
                    public BridgeTable<String> execute() throws Throwable
                    {
                        return doBuildCache(tenantId);
                    }
                }, true, false);

            }
        }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantId));
    }

    private BridgeTable<String> doBuildCache(String tenantId)
    {
        List<AuthorityBridgeLink> links = authorityBridgeDAO.getAuthorityBridgeLinks();
        BridgeTable<String> bridgeTable = new BridgeTable<String>();
        try
        {
            for (AuthorityBridgeLink link : links)
            {
                bridgeTable.addLink(link.getParentName(), link.getChildName());
            }
        }
        catch (ConcurrentModificationException e)
        {
            // Explain exception
            checkCyclic(links);
            // If cyclic groups is not the cause then rethrow
            throw e;
        }
        return bridgeTable;
    }

    private void checkCyclic(List<AuthorityBridgeLink> links)
    {
        Map<String, Set<String>> parentsToChildren = new HashMap<String, Set<String>>();
        for (AuthorityBridgeLink link : links)
        {
            addToMap(parentsToChildren, link.getParentName(), link.getChildName());
        }
        
        Map<String, Set<String>> removed = new HashMap<String, Set<String>>();
        for (String parent : parentsToChildren.keySet())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Start checking from '" + parent + "'");
            }
            Set<String> authorities = new HashSet<String>();
            authorities.add(parent);
            doCheck(parent, parentsToChildren, authorities, removed);
        }
        if (!removed.isEmpty())
        {
            fixCyclic(removed);
            throw new AlfrescoRuntimeException("Cyclic links were detected and removed.");
        }
    }

    private void doCheck(String parent, Map<String, Set<String>> parentsToChildren, Set<String> authorities,
            Map<String, Set<String>> removed)
    {
        Set<String> children = parentsToChildren.get(parent);
        if (children != null)
        {
            for (String child : children)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Check link from '" + parent + "' to '" + child + "'");
                }
                if (isRemoved(removed, parent, child))
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Link from '" + parent + "' to '" + child + "' has been already removed");
                    }
                    continue;
                }
                if (!authorities.add(child))
                {
                    addToMap(removed, parent, child);
                    continue;
                }
                doCheck(child, parentsToChildren, authorities, removed);
                authorities.remove(child);
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Children of '" + parent + "' were processed");
            }
        }
    }

    private boolean isRemoved(Map<String, Set<String>> removed, String parent, String child)
    {
        Set<String> remChildren = removed.get(parent);
        return (remChildren != null && remChildren.contains(child));
    }

    private void addToMap(Map<String, Set<String>> map, String parent, String child)
    {
        Set<String> children = map.get(parent);
        if (children == null)
        {
            children = new HashSet<String>();
            children.add(child);
            map.put(parent, children);
        }
        else
        {
            children.add(child);
        }
    }

    private void fixCyclic(final Map<String, Set<String>> removed)
    {
        // delete cyclic links in new transaction because
        // current cache refresh will be interrupted with AlfrescoRuntimeException
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                for (String parentName : removed.keySet())
                {
                    for (String childName : removed.get(parentName))
                    {
                        // do not refresh authorityBridgeTableCache
                        authorityDAO.removeAuthority(parentName, childName, false);
                        logger.error("Link from '" + parentName + "' to '" + childName +"' was removed to break cycle.");
                    }
                }
                return null;
            }
        }, false, true);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "authorityBridgeDAO", authorityBridgeDAO);
        PropertyCheck.mandatory(this, "retryingTransactionHelper", retryingTransactionHelper);
        PropertyCheck.mandatory(this, "authorityDAO", authorityDAO);
        super.afterPropertiesSet();
    }
}
