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
package org.alfresco.repo.search.impl.solr;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.search.impl.lucene.JSONAPIResultFactory;
import org.alfresco.repo.solr.SOLRAdminClient;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Andy
 */
public class SolrBackupClient implements InitializingBean
{
    private static final Log logger = LogFactory.getLog(SolrBackupClient.class);
    
    // Lock key
    private QName lock;

    private JobLockService jobLockService;

    private String remoteBackupLocation;
    
    private int numberToKeep;

    private String core;
    
    private boolean fixNumberToKeepOffByOneError = false;

    private SOLRAdminClient solrAdminClient;
    
    private SolrQueryClient solrQueryHTTPClient;
    
    
    /**
     * @param fixNumberToKeepOffByOneError the fixNumberToKeepOffByOneError to set
     */
    public void setFixNumberToKeepOffByOneError(boolean fixNumberToKeepOffByOneError)
    {
        this.fixNumberToKeepOffByOneError = fixNumberToKeepOffByOneError;
    }

    public void setSolrAdminClient(SOLRAdminClient solrAdminClient)
    {
        this.solrAdminClient = solrAdminClient;
    }

    public void setCore(String core)
    {
        this.core = core;
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void setRemoteBackupLocation(String remoteBackupLocation)
    {
        this.remoteBackupLocation = remoteBackupLocation;
    }
    
    public void setSolrQueryHTTPClient(SolrQueryClient solrQueryHTTPClient)
    {
        this.solrQueryHTTPClient = solrQueryHTTPClient;
    }

    /**
     * @param numberToKeep the numberToKeep to set
     */
    public void setNumberToKeep(int numberToKeep)
    {
        this.numberToKeep = numberToKeep;
    }

    public void execute()
    {
        if(solrQueryHTTPClient.isSharded())
        {
            return;
        }

        String lockToken = getLock(60000);
        if (lockToken == null)
        {
            return;
        }
        // Use a flag to keep track of the running job
        final AtomicBoolean running = new AtomicBoolean(true);
        jobLockService.refreshLock(lockToken, lock, 30000, new JobLockRefreshCallback()
        {
            @Override
            public boolean isActive()
            {
                return running.get();
            }

            @Override
            public void lockReleased()
            {
                running.set(false);
            }
        });
        try
        {
            executeImpl(running);
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        finally
        {
            // The lock will self-release if answer isActive in the negative
            running.set(false);
            jobLockService.releaseLock(lockToken, lock);
        }
    }

    /**
     * @param running AtomicBoolean
     */
    private void executeImpl(AtomicBoolean running)
    {
        if((remoteBackupLocation == null) || (remoteBackupLocation.length() == 0))
        {
            if(logger.isInfoEnabled())
            {
                logger.info("Back up of SOLR core skipped - no remote backup localtion set for: "+core);
            }
        }
        
        try
        {
            // MNT-6468 fix, ensure that backup job takes at least one second to execute
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // ignore
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("wt","json");
        parameters.put("location", remoteBackupLocation);
        if(fixNumberToKeepOffByOneError)
        {
            parameters.put("numberToKeep", String.valueOf(numberToKeep > 1 ? (numberToKeep + 1) : numberToKeep));
        }
        else
        {
            parameters.put("numberToKeep", String.valueOf(numberToKeep));
        }

        solrAdminClient.executeCommand(core, JSONAPIResultFactory.HANDLER.REPLICATION, JSONAPIResultFactory.COMMAND.BACKUP, parameters);


        if(logger.isInfoEnabled())
        {
            logger.info("Back up of SOLR core completed: "+core);
        }

    }

    private String getLock(long time)
    {
        try
        {
            return jobLockService.getLock(lock, time);
        }
        catch (LockAcquisitionException e)
        {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
       lock  = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "SolrBackupClient-"+core);
        
    }
}
