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
package org.alfresco.repo.search.impl.lucene.fts;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.repo.search.BackgroundIndexerAware;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.SupportsBackgroundIndexing;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Background index update scheduler
 * @author andyh
 *
 */
public class FullTextSearchIndexerImpl implements FTSIndexerAware, FullTextSearchIndexer, DisposableBean
{
    private static Log s_logger = LogFactory.getLog(FullTextSearchIndexerImpl.class);
    
    private static Set<StoreRef> requiresIndex = new LinkedHashSet<StoreRef>();

    private static Set<StoreRef> indexing = new HashSet<StoreRef>();

    private IndexerAndSearcher indexerAndSearcherFactory;
    
    private TransactionService transactionService;

    private int pauseCount = 0;

    private boolean paused = false;

    private int batchSize = 1000;
    
    /**
     * 
     */
    public FullTextSearchIndexerImpl()
    {
        super();
        // System.out.println("Created id is "+this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#requiresIndex(org.alfresco.repo.ref.StoreRef)
     */
    public synchronized void requiresIndex(StoreRef storeRef)
    {
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("FTS index request for "+storeRef);
        }
        requiresIndex.add(storeRef);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#indexCompleted(org.alfresco.repo.ref.StoreRef, int, java.lang.Exception)
     */
    public synchronized void indexCompleted(StoreRef storeRef, int remaining, Throwable t)
    {
        try
        {
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug("FTS index completed for "+storeRef+" ... "+remaining+ " remaining");
            }
            indexing.remove(storeRef);
            if ((remaining > 0) || (t != null))
            {
                requiresIndex(storeRef);
            }
            if (t != null)
            {
                throw new FTSIndexerException(t);
            }
        }
        finally
        {
            this.notifyAll();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#pause()
     */
    public synchronized void pause() throws InterruptedException
    {
        pauseCount++;
        if(s_logger.isTraceEnabled())
        {
            s_logger.trace("..Waiting "+pauseCount+" id is "+this);
        }
        while ((indexing.size() > 0))
        {
            if(s_logger.isTraceEnabled())
            {
                s_logger.trace("Pause: Waiting with count of "+indexing.size()+" id is "+this);
            }
            this.wait();
        }
        pauseCount--;
        if (pauseCount == 0)
        {
            paused = true;
            this.notifyAll(); // only resumers
        }
        if(s_logger.isTraceEnabled())
        {
            s_logger.trace("..Remaining "+pauseCount +" paused = "+paused+" id is "+this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#resume()
     */
    public synchronized void resume() throws InterruptedException
    {
        if (pauseCount == 0)
        {
            if(s_logger.isTraceEnabled())
            {
                s_logger.trace("Direct resume"+" id is "+this);
            }
            paused = false;
        }
        else
        {
            while (pauseCount > 0)
            {
                if(s_logger.isTraceEnabled())
                {
                    s_logger.trace("Resume waiting on "+pauseCount+" id is "+this);
                }
                this.wait();
            }
            paused = false;
        }
    }

    @SuppressWarnings("unused")
    private synchronized boolean isPaused() throws InterruptedException
    {
        if (pauseCount == 0)
        {
            return paused;
        }
        else
        {
            while (pauseCount > 0)
            {
                this.wait();
            }
            return paused;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#index()
     */
    public void index()
    {
        // Use the calling thread to index
        // Parallel indexing via multiple Quartz thread initiating indexing

        int done = 0;
        while (done == 0)
        {
            final StoreRef toIndex = getNextRef();
            try
            {
                if (toIndex != null)
                {
                    if(s_logger.isDebugEnabled())
                    {
                        s_logger.debug("FTS Indexing "+toIndex+" at "+(new java.util.Date()));
                    }
                    try
                    {
                        done += transactionService.getRetryingTransactionHelper().doInTransaction(
                                new RetryingTransactionCallback<Integer>()
                                {
                                    @Override
                                    public Integer execute() throws Throwable
                                    {
                                        Indexer indexer = indexerAndSearcherFactory.getIndexer(toIndex);
                                        // Activate database 'read through' behaviour so that we don't end up with stale
                                        // caches during this potentially long running transaction
                                        indexer.setReadThrough(true);
                                        if (indexer instanceof BackgroundIndexerAware)
                                        {
                                            BackgroundIndexerAware backgroundIndexerAware = (BackgroundIndexerAware) indexer;
                                            backgroundIndexerAware.registerCallBack(FullTextSearchIndexerImpl.this);
                                            return backgroundIndexerAware.updateFullTextSearch(batchSize);
                                        }
                                        return 0;
                                    }
                                }, true);
                    }
                    catch (Exception ex)
                    {
                        indexCompleted(toIndex, 0, ex);
                        if (s_logger.isWarnEnabled())
                        {
                            s_logger.warn("FTS Job threw exception", ex);
                        }
                    }
                }
                else
                {
                    if(s_logger.isTraceEnabled())
                    {
                        s_logger.trace("Nothing to FTS index at "+(new java.util.Date()));
                    }
                    break;
                }
            }
            catch(Throwable t)
            {
                indexCompleted(toIndex, 0, t);
            }
        }
    }

    private synchronized StoreRef getNextRef()
    {
        if (paused || (pauseCount > 0))
        {
            if(s_logger.isTraceEnabled())
            {
                s_logger.trace("Indexing suspended - no store available -  id is "+this);
            }
            return null;
        }

        StoreRef nextStoreRef = null;

        for (StoreRef ref : requiresIndex)
        {
            if (!indexing.contains(ref))
            {
                nextStoreRef = ref;
                // FIFO
                break;
            }
        }

        if (nextStoreRef != null)
        {
            requiresIndex.remove(nextStoreRef);
            indexing.add(nextStoreRef);
        }

        return nextStoreRef;
    }

    /**
     * @param indexerAndSearcherFactory
     */
    public void setIndexerAndSearcherFactory(IndexerAndSearcher indexerAndSearcherFactory)
    {
        this.indexerAndSearcherFactory = indexerAndSearcherFactory;
    }
    
    /**
     * @param transactionService
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException
    {
        @SuppressWarnings("unused")
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:alfresco/application-context.xml");
    }


    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        ListableBeanFactory listableBeanFactory = (ListableBeanFactory)beanFactory;
    
        // Find bean implementaing SupportsBackgroundIndexing and register
        for(Object bgindexable : listableBeanFactory.getBeansOfType(SupportsBackgroundIndexing.class).values())
        {
            if(bgindexable instanceof SupportsBackgroundIndexing)
            {
                ((SupportsBackgroundIndexing)bgindexable).setFullTextSearchIndexer(this);
            }
        }
        
    }

    /**
     * The maximum maximum batch size
     * @param batchSize the batchSize to set
     */
    public void setBatchSize(int batchSzie)
    {
        this.batchSize = batchSzie;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception
    {
        pause();
        requiresIndex.clear();
        indexing.clear();
    }
    
    
}
