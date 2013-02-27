/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.activities.post.cleanup;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;

/**
 * Thr post cleaner component is responsible for purging 'obsolete' activity posts
 */
public class PostCleaner
{
    private static Log logger = LogFactory.getLog(PostCleaner.class);
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(PostCleaner.class.getName());
    
    private int maxAgeMins = 0;
    
    private ActivityPostDAO postDAO;
    private JobLockService jobLockService;
    
    private static final long LOCK_TTL = 60000L;        // 1 minute
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "org.alfresco.repo.activities.post.cleanup.PostCleaner");

    
    public void setPostDAO(ActivityPostDAO postDAO)
    {
        this.postDAO = postDAO;
    }
    
    public void setMaxAgeMins(int mins)
    {
        this.maxAgeMins = mins;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }
    
    public void init()
    {
        checkProperties();
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "postDAO", postDAO);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        
        // check the max age
        if (maxAgeMins <= 0)
        {
            throw new AlfrescoRuntimeException("Property 'maxAgeMins' must be greater than 0");
        }
    }
        
    public void execute() throws JobExecutionException
    {
        final AtomicBoolean keepGoing = new AtomicBoolean(true);
        String lockToken = null;
        try
        {
            // Lock
            lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            // Refresh to get callbacks
            JobLockRefreshCallback callback = new JobLockRefreshCallback()
            {
                @Override
                public void lockReleased()
                {
                    keepGoing.set(false);
                }
            
                @Override
                public boolean isActive()
                {
                    return keepGoing.get();
                }
            };
            jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, callback);
            
            executeWithLock();
        }
        catch (LockAcquisitionException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping post cleaning.  " + e.getMessage());
            }
        }
        finally
        {
            keepGoing.set(false);           // Notify the refresh callback that we are done
            if (lockToken != null)
            {
                jobLockService.releaseLock(lockToken, LOCK_QNAME);
            }
        }
    }
    
    public void executeWithLock() throws JobExecutionException
    {
        checkProperties();
        try
        { 
            long nowTimeOffset = new Date().getTime();
            long keepTimeOffset = nowTimeOffset - (maxAgeMins*60*1000); // millsecs = mins * 60 secs * 1000 msecs
            Date keepDate = new Date(keepTimeOffset);
             
            // clean old entries - PROCESSED - does not clean POSTED or PENDING, which will need to be done manually, if stuck
            int deletedCount = postDAO.deletePosts(keepDate, ActivityPostEntity.STATUS.PROCESSED);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Cleaned " + deletedCount + " entries (upto " + keepDate + ", max age " + maxAgeMins + " mins)");
            }
        }
        catch (SQLException e)
        {
            logger.error("Exception during cleanup of posts", e);
            throw new JobExecutionException(e);
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
                logger.error("Exception during cleanup of posts", e);
            }
        }
    }
}
