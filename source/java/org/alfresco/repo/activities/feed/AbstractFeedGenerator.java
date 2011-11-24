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
package org.alfresco.repo.activities.feed;

import org.alfresco.repo.activities.ActivityPostServiceImpl;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
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
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ActivityFeedGenerator");
    
    /** The time this lock will persist in the database (30 sec but refreshed at regular intervals) */
    private static final long LOCK_TTL = 1000 * 30;
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(AbstractFeedGenerator.class.getName());
    
    private int maxItemsPerCycle = 100;
    
    private ActivityPostDAO postDAO;
    private ActivityPostServiceImpl activityPostServiceImpl;
    private AuthenticationService authenticationService;
    private TransactionService transactionService;
    private JobLockService jobLockService;
    
    private String repoEndPoint; // http://hostname:port/webapp (eg. http://localhost:8080/alfresco)
    
    private boolean userNamesAreCaseSensitive = false;
    
    private RepoCtx ctx = null;
    
    private volatile boolean busy;
    
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
        ctx = new RepoCtx(repoEndPoint);
        ctx.setUserNamesAreCaseSensitive(userNamesAreCaseSensitive);
        
        busy = false;
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
    
    protected boolean isActive()
    {
        return busy;
    }
    
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
        
        String lockToken = null;
        
        try
        {
            JobLockRefreshCallback lockCallback = new LockCallback();
            lockToken = acquireLock(lockCallback);
            
            if (logger.isTraceEnabled())
            {
                logger.trace("Activities feed generator started");
            }
            
            // run one job cycle
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
                logger.debug("Activities feed generator already underway");
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
            releaseLock(lockToken);
        }
    }
    
    protected abstract boolean generate() throws Exception;
    
    private class LockCallback implements JobLockRefreshCallback
    {
        @Override
        public boolean isActive()
        {
            return busy;
        }
        
        @Override
        public void lockReleased()
        {
            // note: currently the cycle will try to complete (even if refresh failed)
            synchronized(this)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("Lock released (refresh failed): " + LOCK_QNAME);
                }
                
                busy = false;
            }
        }
    }
    
    private String acquireLock(JobLockRefreshCallback lockCallback) throws LockAcquisitionException
    {
        // Try to get lock
        String lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
        
        // Got the lock - now register the refresh callback which will keep the lock alive
        jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, lockCallback);
        
        busy = true;
        
        if (logger.isDebugEnabled())
        {
            logger.debug("lock aquired:  " + lockToken);
        }
        
        return lockToken;
    }
    
    private void releaseLock(String lockToken)
    {
        if (lockToken != null)
        {
            busy = false;
            
            jobLockService.releaseLock(lockToken, LOCK_QNAME);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("lock released: " + lockToken);
            }
        }
    }
}
