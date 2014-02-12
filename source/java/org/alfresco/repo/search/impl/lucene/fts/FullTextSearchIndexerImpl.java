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
     * Although not really allowed, locks are being held by daemon threads. This
     * field holds the thread with the lock, so that other threads that 'wait'
     * don't wait forever if a daemon thread that has the lock is killed on shutdown.
     * 
     * On entry to a synchronized method or block the value is either null or the
     * current Thread (if a nested call). On exit it is set back to its original value
     * unless the Thread dies.
     * 
     * Prior to calling wait it must be set to null and on return must be set back to
     * the current thread.
     * 
     * If a daemon thread has just been killed, on return from the wait,
     * it will not be null and the thread holding the lock will not be alive.
     * 
     * The alternative to this approach might include:
     * - Making all threads that use this class daemons (might be simplest)
     */
    private static Thread threadHoldingLock;

    // Helper class to keep track of which Thread has the lock, so we can give up if it dies.
    private abstract class SynchronizedHelper<Result>
    {
        public Result executeNoInterruptedException()
        {
            try {
                return execute();
            } catch (InterruptedException e) {
                // ignore
                return null;
            }
        }
        
        @SuppressWarnings("finally")
        public Result execute() throws InterruptedException
        {
            Thread origThreadHoldingLock = threadHoldingLock;
            threadHoldingLock = Thread.currentThread();
            try
            {
                return run();
            }
            catch (ThreadDeath threadDeath)
            {
                origThreadHoldingLock = null;
                throw threadDeath;
            }
            finally
            {
                threadHoldingLock = origThreadHoldingLock;
            }
        }
        
        public abstract Result run() throws InterruptedException;
        
        // Does a wait(10000). Returns true if the Thread that had the lock has died.
        public boolean waitAndCheckForSubsystemShutdown() throws InterruptedException
        {
            try
            {
                threadHoldingLock = null;
                FullTextSearchIndexerImpl.this.wait(10000);
                return threadHoldingLock != null && !threadHoldingLock.isAlive();
            }
            finally
            {
                threadHoldingLock = Thread.currentThread();
            }
        }
    }

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
    public synchronized void requiresIndex(final StoreRef storeRef)
    {
        new SynchronizedHelper<Void>()
        {
            public Void run()
            {
                if(s_logger.isDebugEnabled())
                {
                    s_logger.debug("FTS index request for "+storeRef);
                }
                requiresIndex.add(storeRef);
                return null;
            }
        }.executeNoInterruptedException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#indexCompleted(org.alfresco.repo.ref.StoreRef, int, java.lang.Exception)
     */
    public synchronized void indexCompleted(final StoreRef storeRef, final int remaining, final Throwable t)
    {
        new SynchronizedHelper<Void>()
        {
            public Void run()
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
                    FullTextSearchIndexerImpl.this.notifyAll();
                }
                return null;
            }
        }.executeNoInterruptedException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#pause()
     */
    public synchronized void pause() throws InterruptedException
    {
        new SynchronizedHelper<Void>()
        {
            public Void run() throws InterruptedException
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
                    
                    if (waitAndCheckForSubsystemShutdown())
                    {
                        indexing.clear();
                    }
                }
                pauseCount--;
                if (pauseCount == 0)
                {
                    paused = true;
                    FullTextSearchIndexerImpl.this.notifyAll(); // only resumers
                }
                if(s_logger.isTraceEnabled())
                {
                    s_logger.trace("..Remaining "+pauseCount +" paused = "+paused+" id is "+this);
                }
                return null;
            }
        }.execute();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#resume()
     */
    public synchronized void resume() throws InterruptedException
    {
        new SynchronizedHelper<Void>()
        {
            public Void run() throws InterruptedException
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

                        if (waitAndCheckForSubsystemShutdown())
                        {
                            break;
                        }
                    }
                    paused = false;
                }
                return null;
            }
        }.execute();
    }

    @SuppressWarnings("unused")
    private synchronized boolean isPaused() throws InterruptedException
    {
        return new SynchronizedHelper<Boolean>()
        {
            public Boolean run() throws InterruptedException
            {
                if (pauseCount == 0)
                {
                    return paused;
                }
                else
                {
                    while (pauseCount > 0)
                    {
                        if (waitAndCheckForSubsystemShutdown())
                        {
                            break;
                        }
                    }
                    return paused;
                }
            }
        }.execute();
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
        return new SynchronizedHelper<StoreRef>()
        {
            public StoreRef run()
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
        }.executeNoInterruptedException();
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
