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
package org.alfresco.repo.activities.feed.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.activities.feed.AbstractFeedGenerator;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.activities.feed.JobSettings;
import org.alfresco.repo.activities.feed.RepoCtx;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The local (ie. not grid) feed generator component is responsible for generating feed entries
 */
public class LocalFeedGenerator extends AbstractFeedGenerator
{
    private static Log logger = LogFactory.getLog(LocalFeedGenerator.class);
   
    private FeedTaskProcessor feedTaskProcessor;

    private int batchSize = 1000;
    private int numThreads = 4;
    
    public void setFeedTaskProcessor(FeedTaskProcessor feedTaskProcessor)
    {
        this.feedTaskProcessor = feedTaskProcessor;
    }
    
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    public void setNumThreads(int numThreads)
    {
        this.numThreads = numThreads;
    }

    @Override
    public int getEstimatedGridSize()
    {
        return 1;
    }
    
    public void init() throws Exception
    {
       super.init();
    }

    protected boolean generate() throws Exception
    {
        final Long maxSequence = getPostDaoService().getMaxActivitySeq();
        final Long minSequence = getPostDaoService().getMinActivitySeq();
        final Integer maxNodeHash = getPostDaoService().getMaxNodeHash();

        if ((maxSequence == null) || (minSequence == null) || (maxNodeHash == null))
        {
            return false;
        }
        
        // TODO ... or push this upto to job scheduler ... ?
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                getWebScriptsCtx().setTicket(getAuthenticationService().getCurrentTicket());
                return null;
            }
        }, AuthenticationUtil.getSystemUserName()); // need web scripts to support System-level authentication ... see RepositoryContainer !
        
        // process the activity posts using the batch processor {@link BatchProcessor}
        BatchProcessor.BatchProcessWorker<JobSettings> worker = new BatchProcessor.BatchProcessWorker<JobSettings>()
        {
            @Override
            public String getIdentifier(final JobSettings js)
            {
                // TODO
                StringBuilder sb = new StringBuilder("JobSettings ");
                sb.append(js);
                return sb.toString();
            }

            @Override
            public void beforeProcess() throws Throwable
            {
            }

            @Override
            public void afterProcess() throws Throwable
            {
            }

            @Override
            public void process(final JobSettings js) throws Throwable
            {
                final RetryingTransactionHelper txHelper = getTransactionService().getRetryingTransactionHelper();
                txHelper.setMaxRetries(0);

                txHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        int jobTaskNode = js.getJobTaskNode();
                        long minSeq = js.getMinSeq();
                        long maxSeq = js.getMaxSeq();
                        RepoCtx webScriptsCtx = js.getWebScriptsCtx();
                        
                        // FeedTaskProcessor takes JobSettings parameters instead collection of ActivityPost. FeedTaskProcessor can be refactored.
                        feedTaskProcessor.process(jobTaskNode , minSeq , maxSeq , webScriptsCtx );
                        return null;
                    }
                }, false, true);
            }
        };
        
        // provides a JobSettings object
        BatchProcessWorkProvider<JobSettings> provider = new BatchProcessWorkProvider<JobSettings>()
        {
            private Long skip = minSequence;
            private boolean hasMore = true;

            @Override
            public int getTotalEstimatedWorkSize()
            {
                long size = maxSequence - minSequence + 1;
                long remain = size % batchSize;
                long workSize = (remain == 0) ? (size / batchSize) : (size / batchSize + 1);
                return (int) workSize;
            }

            @Override
            public Collection<JobSettings> getNextWork()
            {
                if (!hasMore)
                {
                    return Collections.emptyList();
                }
                
                JobSettings js = new JobSettings();
                js.setMinSeq(skip);
                js.setMaxSeq(skip + batchSize - 1);
                js.setJobTaskNode(maxNodeHash);
                js.setWebScriptsCtx(getWebScriptsCtx());
                
                skip += batchSize;
                hasMore = skip > maxSequence ? false : true;
                
                // One JobSettings object will be returned. Because FeedTaskProcessor fetches list activity posts by itself before processing.
                List<JobSettings> result = new ArrayList<JobSettings>(1);
                result.add(js);
                
                return result;
            }
        };
        
        final RetryingTransactionHelper txHelper = getTransactionService().getRetryingTransactionHelper();
        txHelper.setMaxRetries(0);

        // batchSize and loggingInterval parameters are equal 1 because provider always will provide collection with one JobSettings object. 
        // FeedTaskProcessor fetches list activity posts by itself before processing. It needs only JobSettings parameters. FeedTaskProcessor can be refactored.
        new BatchProcessor<JobSettings>(
                "LocalFeedGenerator",
                txHelper,
                provider,
                numThreads, 1,
                null,
                logger, 1).process(worker, true);
        
        return true;
    }
        
}
