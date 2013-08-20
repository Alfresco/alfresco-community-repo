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
package org.alfresco.repo.usage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.usage.UsageDAO;
import org.alfresco.repo.domain.usage.UsageDAO.MapHandler;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.usage.UsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * User Usage Tracking Component - to allow user usages to be collapsed or re-calculated
 * 
 * - used by UserUsageCollapseJob to collapse usage deltas.
 * - used on bootstrap to either clear all usages or (re-)calculate all missing usages.
 */
public class UserUsageTrackingComponent extends AbstractLifecycleBean implements NodeServicePolicies.OnCreateNodePolicy
{
    private static Log logger = LogFactory.getLog(UserUsageTrackingComponent.class);
    
    private TransactionServiceImpl transactionService;
    private ContentUsageImpl contentUsageImpl;
    
    private NodeService nodeService;
    private UsageDAO usageDAO;
    private UsageService usageService;
    private TenantAdminService tenantAdminService;
    private TenantService tenantService;
    private JobLockService jobLockService;
    
    private StoreRef personStoreRef;
    
    private int clearBatchSize = 50;
    private int updateBatchSize = 50;
    
    private boolean enabled = true;
    private static final long LOCK_TTL = 60000L;        // 1 minute
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "org.alfresco.repo.usage.UserUsageTrackingComponent");
    
    private PolicyComponent policyComponent;

    /**
     * Spring bean init method
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        if (enabled)
        {
            this.policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode"));
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setTransactionService(TransactionServiceImpl transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setContentUsageImpl(ContentUsageImpl contentUsageImpl)
    {
        this.contentUsageImpl = contentUsageImpl;
    }
    
    public void setPersonStoreUrl(String storeUrl)
    {
        this.personStoreRef = new StoreRef(storeUrl);
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setUsageDAO(UsageDAO usageDAO)
    {
        this.usageDAO = usageDAO;
    }
    
    public void setUsageService(UsageService usageService)
    {
        this.usageService = usageService;
    }
    
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setClearBatchSize(int clearBatchSize)
    {
        this.clearBatchSize = clearBatchSize;
    }
    
    public void setUpdateBatchSize(int updateBatchSize)
    {
        this.updateBatchSize = updateBatchSize;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    private class TrackingJobLockRefreshCallback implements JobLockRefreshCallback
    {
        private final AtomicBoolean running = new AtomicBoolean(true);

        @Override
        public boolean isActive()
        {
            return running.get();
        }

        public void stopRefreshing()
        {
            running.set(false);
        }

        @Override
        public void lockReleased()
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("lock released");
            }
        }
    };

    private String getLock(long time)
    {
        try
        {
            return jobLockService.getLock(LOCK_QNAME, time);
        }
        catch (LockAcquisitionException e)
        {
            return null;
        }
    }

    public void execute()
    {
        if (enabled == false || transactionService.isReadOnly())
        {
            return;
        }
        
        String lockToken = getLock(LOCK_TTL);
        if (lockToken != null)
        {
            TrackingJobLockRefreshCallback callback = new TrackingJobLockRefreshCallback();
            jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, callback);
            // collapse usages - note: for MT environment, will collapse for all tenants
            try
            {
                collapseUsages();
            }
            finally
            {
                // Release the locks on the job and stop refreshing
                callback.stopRefreshing();
                jobLockService.releaseLock(lockToken, LOCK_QNAME);
            }
        }
    }
    
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // default domain
        bootstrapInternal();
        
        if (tenantAdminService.isEnabled())
        {
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        bootstrapInternal();
                        return null;
                    }
                }, tenant.getTenantDomain());
            }
       }
    }
    
    public void bootstrapInternal()
    {
        if (transactionService.isReadOnly())
        {
            return;
        }
        
        String lockToken = getLock(LOCK_TTL);
        if (lockToken != null)
        {
            TrackingJobLockRefreshCallback callback = new TrackingJobLockRefreshCallback();
            jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, callback);
            try
            {
                if (enabled)
                {
                    // enabled - calculate missing usages
                    calculateMissingUsages();
                }
                else
                {
                    if (clearBatchSize != 0)
                    {
                        // disabled - remove all usages
                        clearAllUsages();
                    }
                }
            }
            finally
            {
                callback.stopRefreshing();
                jobLockService.releaseLock(lockToken, LOCK_QNAME);
            }
        }
    }
    
    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        if (enabled == true)
        {
            final NodeRef personRef = childAssocRef.getChildRef();
            final String userName = (String) this.nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
            
            if (userName != null)
            {
                RetryingTransactionCallback<Long> updateUserWithUsage = new RetryingTransactionCallback<Long>()
                {
                    public Long execute() throws Throwable
                    {
                        List<String> stores = contentUsageImpl.getStores();
                        
                        Long currentUsage = null;
                        
                        for (String store : stores)
                        {
                            final StoreRef storeRef = tenantService.getName(new StoreRef(store));
                            
                            Long contentSize = usageDAO.getContentSizeForStoreForUser(storeRef, userName);
                            
                            if (contentSize != null)
                            {
                                currentUsage = (currentUsage == null ? 0L : currentUsage) + contentSize;
                            }
                        }
                        
                        if (currentUsage != null && currentUsage > 0L)
                        {
                            List<Pair<NodeRef, Long>> batchUserUsages = new ArrayList<Pair<NodeRef, Long>>(1);
                            batchUserUsages.add(new Pair<NodeRef, Long>(personRef, currentUsage));
                            
                            updateUsages(batchUserUsages);
                        }
                        
                        return null;
                    }
                };
                
                // execute in READ-WRITE txn
                transactionService.getRetryingTransactionHelper().doInTransaction(updateUserWithUsage, false);
            }
        }
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
    
    
    /**
     * Clear content usage for all users that have a usage.
     */
    private void clearAllUsages()
    {
        if (logger.isInfoEnabled()) 
        {
            logger.info("Disabled - clear non-missing user usages ...");
        }
        
        final Map<String, NodeRef> users = new HashMap<String, NodeRef>();
        
        RetryingTransactionCallback<Object> getUsersWithUsage = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // get people (users) with calculated usage
                MapHandler userHandler = new MapHandler()
                {
                    public void handle(Map<String, Object> result)
                    {
                        String username = (String)result.get("username");
                        String uuid = (String)result.get("uuid");
                        
                        if (! username.equalsIgnoreCase(AuthenticationUtil.getSystemUserName()))
                        {
                            users.put(username, new NodeRef(personStoreRef, uuid));
                        }
                    }
                };
                usageDAO.getUsersWithUsage(tenantService.getName(personStoreRef), userHandler);
                
                return null;
            }
        };
        
        // execute in READ-ONLY txn
        transactionService.getRetryingTransactionHelper().doInTransaction(getUsersWithUsage, true);
        
        if (logger.isInfoEnabled()) 
        {
            logger.info("Found " + users.size() + " users to clear");
        }
        
        int clearCount = 0;
        int batchCount = 0;
        int totalCount = 0;
        
        List<NodeRef> batchPersonRefs = new ArrayList<NodeRef>(clearBatchSize);
        for (NodeRef personNodeRef : users.values())
        {
            batchPersonRefs.add(personNodeRef);
            batchCount++;
            totalCount++;
            
            if ((batchCount == clearBatchSize) || (totalCount == users.size()))
            {
                int cleared = clearUsages(batchPersonRefs);
                clearCount = clearCount + cleared;
                
                batchPersonRefs.clear();
                batchCount = 0;
            }
        }
        
        if (logger.isInfoEnabled()) 
        {
            logger.info("... cleared non-missing usages for " + clearCount + " users");
        }
    }
    
    private int clearUsages(final List<NodeRef> personNodeRefs)
    {
        RetryingTransactionCallback<Integer> clearPersonUsage = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                int clearCount = 0;
                for (NodeRef personNodeRef : personNodeRefs)
                {
                    nodeService.setProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT, null);
                    usageService.deleteDeltas(personNodeRef);
                    
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Cleared usage for person ("+ personNodeRef+")");
                    }
                    
                    clearCount++;
                }
                return clearCount;
            }
        };
        
        // execute in READ-WRITE txn
        return transactionService.getRetryingTransactionHelper().doInTransaction(clearPersonUsage, false);
    }
    
    /**
     * Recalculate content usage for all users that have no usage. 
     * Required if upgrading an existing Alfresco, for users that have not had their initial usage calculated.
     */
    private void calculateMissingUsages()
    {
        if (logger.isInfoEnabled()) 
        {
            logger.info("Enabled - calculate missing user usages ...");
        }
        
        final Map<String, NodeRef> users = new HashMap<String, NodeRef>();
        
        RetryingTransactionCallback<Object> getUsersWithoutUsage = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // get people (users) without calculated usage
                MapHandler userHandler = new MapHandler()
                {
                    public void handle(Map<String, Object> result)
                    {
                        String username = (String)result.get("username");
                        String uuid = (String)result.get("uuid");
                        
                        if (! username.equalsIgnoreCase(AuthenticationUtil.getSystemUserName()))
                        {
                            users.put(username, new NodeRef(personStoreRef, uuid));
                        }
                    }
                };
                
                usageDAO.getUsersWithoutUsage(tenantService.getName(personStoreRef), userHandler);
                        
                return null;
            }
        };
        
        // execute in READ-ONLY txn
        transactionService.getRetryingTransactionHelper().doInTransaction(getUsersWithoutUsage, true);
        
        if (logger.isInfoEnabled()) 
        {
            logger.info("Found " + users.size() + " users to recalculate");
        }
        
        int updateCount = 0;
        if (users.size() > 0)
        {
            updateCount = recalculateUsages(users);
        }
        
        if (logger.isInfoEnabled()) 
        {
            logger.info("... calculated missing usages for " + updateCount + " users");
        }
    }
    
    /*
     * Recalculate content usage for given users. Required if upgrading an existing Alfresco, for users that
     * have not had their initial usage calculated. In a future release, could also be called explicitly by
     * a SysAdmin, eg. via a JMX operation.
     */
    private int recalculateUsages(final Map<String, NodeRef> users)
    {
        final Map<String, Long> currentUserUsages = new HashMap<String, Long>(users.size());
        
        RetryingTransactionCallback<Long> calculateCurrentUsages = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                List<String> stores = contentUsageImpl.getStores();
                 
                for (String store : stores)
                {
                    final StoreRef storeRef = tenantService.getName(new StoreRef(store));
                    
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Recalc usages for store=" + storeRef);
                    }
                    
                    // get content urls
                    MapHandler userContentUrlHandler = new MapHandler()
                    {
                        public void handle(Map<String, Object> result)
                        {
                            String owner = (String)result.get("owner");
                            String creator = (String)result.get("creator");
                            Long contentSize = (Long)result.get("contentSize"); // sum of content size (via join of new style content id)
                            
                            if (contentSize == null)
                            {
                                contentSize = 0L;
                            }
                            
                            if (owner == null)
                            {
                                owner = creator;
                            }
                            
                            Long currentUsage = currentUserUsages.get(owner);
                            
                            if (currentUsage == null)
                            {
                                currentUsage = 0L;
                            }
                            
                            currentUserUsages.put(owner, currentUsage + contentSize);
                        }
                    };
                    
                    // Query and sum the 'new' style content properties
                    usageDAO.getUserContentSizesForStore(storeRef, userContentUrlHandler);
                }
                
                return null;
            }
        };
        
        // execute in READ-ONLY txn
        transactionService.getRetryingTransactionHelper().doInTransaction(calculateCurrentUsages, true);
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Usages calculated - start update");
        }
        
        int updateCount = 0;
        int batchCount = 0;
        int totalCount = 0;
        
        List<Pair<NodeRef, Long>> batchUserUsages = new ArrayList<Pair<NodeRef, Long>>(updateBatchSize);
        
        for (Map.Entry<String, NodeRef> user : users.entrySet())
        {
            String userName = user.getKey();
            NodeRef personNodeRef = user.getValue();
            
            Long currentUsage = currentUserUsages.get(userName);
            if (currentUsage == null)
            {
                currentUsage = 0L;
            }
            
            batchUserUsages.add(new Pair<NodeRef, Long>(personNodeRef, currentUsage));
            batchCount++;
            totalCount++;
            
            if ((batchCount == updateBatchSize) || (totalCount == users.size()))
            {
                int updated = updateUsages(batchUserUsages);
                updateCount = updateCount + updated;
                
                batchUserUsages.clear();
                batchCount = 0;
            }
        }
        
        return totalCount;
    }
    
    private int updateUsages(final List<Pair<NodeRef, Long>> userUsages)
    {
        RetryingTransactionCallback<Integer> updateCurrentUsages = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                int updateCount = 0;
                
                for (Pair<NodeRef, Long> userUsage : userUsages)
                {
                    NodeRef personNodeRef = userUsage.getFirst();
                    Long currentUsage = userUsage.getSecond();
                    
                    contentUsageImpl.setUserStoredUsage(personNodeRef, currentUsage);
                    usageService.deleteDeltas(personNodeRef);
                    
                    updateCount++;
                }
                return updateCount;
            }
        };
        
        // execute in READ-WRITE txn
        return transactionService.getRetryingTransactionHelper().doInTransaction(updateCurrentUsages, false);
    }
    
    /**
     * Collapse usages - note: for MT environment, will collapse all tenants
     */
    private void collapseUsages()
    {
        if (logger.isDebugEnabled()) 
        {
            logger.debug("Collapse usages ...");
        }
        
        // Collapse usage deltas (if a person has initial usage set)
        RetryingTransactionCallback<Set<NodeRef>> getUsageNodeRefs = new RetryingTransactionCallback<Set<NodeRef>>()
        {
            public Set<NodeRef> execute() throws Throwable
            {
                // Get distinct candidates
                return usageService.getUsageDeltaNodes();
            }
        };
        
        // execute in READ-ONLY txn
        Set<NodeRef> usageNodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(getUsageNodeRefs, true);
        
        int collapseCount = 0;
        for (final NodeRef usageNodeRef : usageNodeRefs)
        {
            Boolean collapsed = TenantUtil.runAsSystemTenant(new TenantRunAsWork<Boolean>()
            {
                public Boolean doWork() throws Exception
                {
                    return collapseUsage(usageNodeRef);
                }
            }, tenantService.getDomain(usageNodeRef.getStoreRef().getIdentifier()));
            
            if (collapsed)
            {
                collapseCount++;
            }
        }
        
        if (logger.isDebugEnabled()) 
        {
            logger.debug("... collapsed usages for " + collapseCount + " users");
        }
    }
    
    private boolean collapseUsage(final NodeRef usageNodeRef)
    {
        RetryingTransactionCallback<Boolean> collapseUsages = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute() throws Throwable
            {
                if (!nodeService.exists(usageNodeRef))
                {
                    // Ignore
                    return false;
                }
                QName nodeType = nodeService.getType(usageNodeRef);
                
                if (nodeType.equals(ContentModel.TYPE_PERSON))
                {
                    NodeRef personNodeRef = usageNodeRef;
                    String userName = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
                    
                    long currentUsage = contentUsageImpl.getUserStoredUsage(personNodeRef);
                    if (currentUsage != -1)
                    {
                        // Collapse the usage deltas
                        // Calculate and remove deltas in one go to guard against deletion of
                        // deltas from another transaction that have not been included in the
                        // calculation
                        currentUsage = contentUsageImpl.getUserUsage(personNodeRef, true);
                        contentUsageImpl.setUserStoredUsage(personNodeRef, currentUsage);
                        
                        if (logger.isTraceEnabled()) 
                        {
                            logger.trace("Collapsed usage: username=" + userName + ", usage=" + currentUsage);
                        }
                    }
                    else
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn("Initial usage for user has not yet been calculated: " + userName);
                        }
                    }
                }
                return true;
            }
        };
        
        // execute in READ-WRITE txn
        return transactionService.getRetryingTransactionHelper().doInTransaction(collapseUsages, false);
    }
}
