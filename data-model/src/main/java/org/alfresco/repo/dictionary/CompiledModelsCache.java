/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.dictionary;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.cache.AbstractAsynchronouslyRefreshedCache;
import org.alfresco.util.cache.RefreshableCacheEvent;
import org.alfresco.util.cache.RefreshableCacheListener;
import org.alfresco.util.cache.RefreshableCacheRefreshedEvent;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * Asynchronously refreshed cache for dictionary models.
 */
public class CompiledModelsCache extends AbstractAsynchronouslyRefreshedCache<DictionaryRegistry>
{
    private static Log logger = LogFactory.getLog(CompiledModelsCache.class);

    private static final String POST_TRANSACTION_PENDING_REFRESH_REQUESTS = "postTransactionPendingCMRefreshRequests";
    private static final String RESOURCE_KEY_TXN_DATA = "CompiledModelsCache.TxCMBReEv";

    private DictionaryDAOImpl dictionaryDAO;
    private TenantService tenantService;

    private String broadcastKeyTxnData;
    private boolean bootstrapping = true;

    @Override
    protected DictionaryRegistry buildCache(String tenantId)
    {
        if (tenantId == null)
        {
            tenantId = tenantService.getCurrentUserDomain();
        }

        final String finalTenant = tenantId;
        return AuthenticationUtil.runAs(new RunAsWork<DictionaryRegistry>()
        {
            public DictionaryRegistry doWork() throws Exception
            {
                return dictionaryDAO.initDictionaryRegistry(finalTenant);
            }
        }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantId));
    }

    /**
     * @param tenantId the tenantId of cache that will be removed from live cache
     * @return removed DictionaryRegistry
     */
    public void remove(final String tenantId)
    {
        //TODO Should be reworked when ACE-2001 will be implemented
        liveLock.writeLock().lock();
        try
        {
            DictionaryRegistry dictionaryRegistry = live.get(tenantId);
            if (dictionaryRegistry != null)
            {
                live.remove(tenantId);
                dictionaryRegistry.remove();
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Removed dictionary register for tenant " + tenantId);
                }
            }
        }
        finally
        {
            liveLock.writeLock().unlock();
        }
    }

    /**
     * @param dictionaryDAO the dictionaryDAOImpl to set
     */
    public void setDictionaryDAO(DictionaryDAOImpl dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    /**
     * @param tenantService the tenantService to set
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();

        broadcastKeyTxnData = RESOURCE_KEY_TXN_DATA + "BC." + getCacheId();

        // RefreshableCacheListener as anonymous class since CompileModelsCache already
        // implements this interface, but expects to be invoked in different circumstances.
        register(new RefreshableCacheListener()
        {
            @Override
            public void onRefreshableCacheEvent(RefreshableCacheEvent event)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Handling "+event.getClass().getSimpleName()+
                                ", cache="+event.getCacheId()+
                                ", key="+event.getKey());
                }
                
                if (event instanceof RefreshableCacheRefreshedEvent &&
                    event.getCacheId().equals(getCacheId()))
                {
                    // notify registered listeners that dictionary has been initialised (population is complete).
                    // Note we do that here to ensure that the dictionary registry has been added to the cache,
                    // so that any dependencies (like the CMIS dictionary) will use the new dictionary.
                    for (DictionaryListener dictionaryListener : dictionaryDAO.getDictionaryListeners())
                    {
                        logger.debug("Calling afterDIctionaryInit ["+event.getClass().getSimpleName()+
                                ", cache="+event.getCacheId()+
                                ", key="+event.getKey()+
                                "] on "+
                                dictionaryListener.getClass().getSimpleName());

                        dictionaryListener.afterDictionaryInit();
                    }
                }
            }

            @Override
            public String getCacheId()
            {
                return CompiledModelsCache.this.getCacheId();
            }
        }); 
    }

    public void forceInChangesForThisUncommittedTransaction(String key)
    {
        super.forceInChangesForThisUncommittedTransaction(key);

        if (!bootstrapping) 
        {
            broadcastRefresh(key);
        }
        else 
        {
            bootstrapping = false;
        }
    }
    
    private void broadcastRefresh(String key)
    {
        if (TransactionSupportUtil.getTransactionId() != null && TransactionSynchronizationManager.isSynchronizationActive())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Compiled models cache adding" + key + " to post commit list: " + this);
            }

            String currentTxn = TransactionSupportUtil.getTransactionId();
            TransactionListener transactionListener = new TransactionListener("TxCMBReEv" + currentTxn);
            TransactionSupportUtil.bindListener(transactionListener, 0);

            LinkedHashSet<String> broadcastKeys = (LinkedHashSet<String>) TransactionSupportUtil.getResource(broadcastKeyTxnData);
            if (broadcastKeys == null)
            {
                broadcastKeys = new LinkedHashSet<>();
                TransactionSupportUtil.bindResource(broadcastKeyTxnData, broadcastKeys);
            }

            broadcastKeys.add(key);
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Compiled models cache broadcastRefresh for key - " + key + " for cache - " + getCacheId());
            }

            refresh(key);
        }
    }

    private class TransactionListener extends TransactionListenerAdapter
    {
        private final String id;

        TransactionListener(String uniqueId)
        {
            this.id = uniqueId;

            if (logger.isDebugEnabled())
            {
                logger.debug("Created lister with id = " + id);
            }
        }

        @Override
        public void afterCommit()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Starting aftercommit listener execution.");
            }

            LinkedHashSet<String> broadcastKeys = (LinkedHashSet<String>) TransactionSupportUtil.getResource(broadcastKeyTxnData);
            if (broadcastKeys != null)
            {
                for (String key : broadcastKeys)
                {
                    try
                    {
                        refresh(key);
                    }
                    catch (Exception e)
                    {
                        logger.error("The after commit callback " + id + " failed to execute: " + e.getMessage(), e);
                        // consume exception
                    }
                }
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof CompiledModelsCache.TransactionListener))
            {
                return false;
            }
            CompiledModelsCache.TransactionListener that = (CompiledModelsCache.TransactionListener) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id);
        }
    }
}
