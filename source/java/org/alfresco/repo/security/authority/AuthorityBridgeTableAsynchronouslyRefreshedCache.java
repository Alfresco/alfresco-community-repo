/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority;

import java.util.List;

import org.alfresco.repo.cache.AbstractAsynchronouslyRefreshedCache;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.BridgeTable;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Andy
 * @since 4.1.3
 */
public class AuthorityBridgeTableAsynchronouslyRefreshedCache extends AbstractAsynchronouslyRefreshedCache<BridgeTable<String>> implements InitializingBean
{
    private AuthorityBridgeDAO authorityBridgeDAO;
    private RetryingTransactionHelper retryingTransactionHelper;

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

    @Override
    protected BridgeTable<String> buildCache(final String tenantId)
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

    private BridgeTable<String> doBuildCache(String tenantId)
    {
        List<AuthorityBridgeLink> links = authorityBridgeDAO.getAuthorityBridgeLinks();
        BridgeTable<String> bridgeTable = new BridgeTable<String>();
        for (AuthorityBridgeLink link : links)
        {
            bridgeTable.addLink(link.getParentName(), link.getChildName());
        }
        return bridgeTable;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "authorityBridgeDAO", authorityBridgeDAO);
        PropertyCheck.mandatory(this, "retryingTransactionHelper", retryingTransactionHelper);
        super.afterPropertiesSet();
    }
}
