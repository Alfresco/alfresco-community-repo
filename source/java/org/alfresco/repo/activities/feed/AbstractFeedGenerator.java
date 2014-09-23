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
package org.alfresco.repo.activities.feed;

import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.repo.activities.ActivityPostServiceImpl;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;

/**
 * Implementations of the abstract feed generator component are responsible for generating activity feed entries
 */
public abstract class AbstractFeedGenerator implements FeedGenerator
{
    private static Log logger = LogFactory.getLog(AbstractFeedGenerator.class);

    /** The name of the lock used to ensure that feed generator does not run on more than one node at the same time */
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "FeedGenerator");
    
    /** The time this lock will persist in the database (60 sec but refreshed at regular intervals) */
    private static final long LOCK_TTL = 1000 * 60;
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(AbstractFeedGenerator.class.getName());
    
    private int maxItemsPerCycle = 100;
    
    private ActivityPostDAO postDAO;
    private ActivityPostServiceImpl activityPostServiceImpl;
    private AuthenticationService authenticationService;
    private SysAdminParams sysAdminParams;
    private TransactionService transactionService;
    private JobLockService jobLockService;
    
    private String repoEndPoint; // http://hostname:port/webapp (eg. http://localhost:8080/alfresco)
    
    private boolean userNamesAreCaseSensitive = false;
    
    private RepoCtx ctx = null;
    
    public void setActivityPostServiceImpl(ActivityPostServiceImpl activityPostServiceImpl)
    {
        this.activityPostServiceImpl = activityPostServiceImpl;
    }
    
    public void setPostDAO(ActivityPostDAO postDAO)
    {
        this.postDAO = postDAO;
    }
    
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    public void setRepoEndPoint(String repoEndPoint)
    {
        this.repoEndPoint = repoEndPoint;
    }
    
    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }
    
    public void setMaxItemsPerCycle(int maxItemsPerCycle)
    {
        this.maxItemsPerCycle = maxItemsPerCycle;
    }
    
    public int getMaxItemsPerCycle()
    {
        return this.maxItemsPerCycle;
    }
    
    public ActivityPostDAO getPostDaoService()
    {
        return this.postDAO;
    }
    
    public AuthenticationService getAuthenticationService()
    {
        return this.authenticationService;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }
   
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public TransactionService getTransactionService()
    {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public RepoCtx getWebScriptsCtx()
    {
        return this.ctx;
    }
     
    public void init() throws Exception
    {
        ctx = new RepoCtx(sysAdminParams, repoEndPoint);
        ctx.setUserNamesAreCaseSensitive(userNamesAreCaseSensitive);
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "postDAO", postDAO);
        
        activityPostServiceImpl.setEstimatedGridSize(getEstimatedGridSize());
    }
     
    abstract public int getEstimatedGridSize();
    
    public void execute() throws JobExecutionException
    {
        checkProperties();
        
        // Avoid running when in read-only mode
        if (!transactionService.getAllowWrite())
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Activities feed generator not running due to read-only server");
            }
            return;
        }

        //MNT-12145 : BM-0013 Soak test: Exception during generation of feeds org.springframework.dao.DataIntegrityViolationException.
        // run one job cycle
        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        
        helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {

                LockCallback lockCallback = new LockCallback();

                String lockToken = null;
                try
                {
                    lockToken = acquireLock(lockCallback);

                    // lock held here

                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Activities feed generator started");
                    }

                    generate();

                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Activities feed generator completed");
                    }
                }
                catch (LockAcquisitionException e)
                {
                    // Job being done by another process
                    if (logger.isDebugEnabled())
                    {
                logger.debug("Activities feed generator already underway: " + LOCK_QNAME);
                    }
                }
                catch (Throwable e)
                {
                    // If the VM is shutting down, then ignore
                    if (vmShutdownListener.isVmShuttingDown())
                    {
                        // Ignore
                    }
                    else
                    {
                        logger.error("Exception during generation of feeds", e);
                    }
                }
                finally
                {
                    releaseLock(lockCallback, lockToken);
                }
                return null;
            }
        }, false, false);
    }
    
    protected abstract boolean generate() throws Exception;

    private class LockCallback implements JobLockRefreshCallback
    {
        final AtomicBoolean running = new AtomicBoolean(true);
        
        @Override
        public boolean isActive()
        {
            return running.get();
        }
        
        @Override
        public void lockReleased()
        {
            running.set(false);
            if (logger.isDebugEnabled())
            {
                logger.debug("Lock release notification: " + LOCK_QNAME);
            }
        }
    }
    
    private String acquireLock(JobLockRefreshCallback lockCallback) throws LockAcquisitionException
    {
        // Try to get lock
        String lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
        
        // Got the lock - now register the refresh callback which will keep the lock alive
        jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, lockCallback);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("lock acquired: " + LOCK_QNAME + ": " + lockToken);
        }
        
        return lockToken;
    }
    
    private void releaseLock(LockCallback lockCallback, String lockToken)
    {
        try
        {
            if (lockCallback != null)
            {
                lockCallback.running.set(false);
            }
            
            if (lockToken != null)
            { 
                jobLockService.releaseLock(lockToken, LOCK_QNAME);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Lock released: " + LOCK_QNAME + ": " + lockToken);
                }
            }
        }
        catch (LockAcquisitionException e)
        {
            // Ignore
            if (logger.isDebugEnabled())
            {
                logger.debug("Lock release failed: " + LOCK_QNAME + ": " + lockToken + "(" + e.getMessage() + ")");
            }
        }
    }
}