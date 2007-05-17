/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.lucene.fts;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.repo.search.BackgroundIndexerAware;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.SupportsBackgroundIndexing;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Background index update scheduler
 * @author andyh
 *
 */
public class FullTextSearchIndexerImpl implements FTSIndexerAware, FullTextSearchIndexer
{
    private static Set<StoreRef> requiresIndex = new LinkedHashSet<StoreRef>();

    private static Set<StoreRef> indexing = new HashSet<StoreRef>();

    private IndexerAndSearcher indexerAndSearcherFactory;

    private int pauseCount = 0;

    private boolean paused = false;

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
        requiresIndex.add(storeRef);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer#indexCompleted(org.alfresco.repo.ref.StoreRef, int, java.lang.Exception)
     */
    public synchronized void indexCompleted(StoreRef storeRef, int remaining, Exception e)
    {
        try
        {
            indexing.remove(storeRef);
            if ((remaining > 0) || (e != null))
            {
                requiresIndex(storeRef);
            }
            if (e != null)
            {
                throw new FTSIndexerException(e);
            }
        }
        finally
        {
            // System.out.println("..Index Complete: id is "+this);
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
        // System.out.println("..Waiting "+pauseCount+" id is "+this);
        while ((indexing.size() > 0))
        {
            // System.out.println("Pause: Waiting with count of "+indexing.size()+" id is "+this);
            this.wait();
        }
        pauseCount--;
        if (pauseCount == 0)
        {
            paused = true;
            this.notifyAll(); // only resumers
        }
        // System.out.println("..Remaining "+pauseCount +" paused = "+paused+" id is "+this);
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
            // System.out.println("Direct resume"+" id is "+this);
            paused = false;
        }
        else
        {
            while (pauseCount > 0)
            {
                // System.out.println("Reusme waiting on "+pauseCount+" id is "+this);
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
            StoreRef toIndex = getNextRef();
            if (toIndex != null)
            {
                // System.out.println("Indexing "+toIndex+" at "+(new java.util.Date()));
                Indexer indexer = indexerAndSearcherFactory.getIndexer(toIndex);
                if(indexer instanceof BackgroundIndexerAware)
                {
                   BackgroundIndexerAware backgroundIndexerAware = (BackgroundIndexerAware)indexer;
                   backgroundIndexerAware.registerCallBack(this);
                   done += backgroundIndexerAware.updateFullTextSearch(1000);
                }
            }
            else
            {
                break;
                // System.out.println("Nothing to Indexing at "+(new java.util.Date()));
            }
        }
    }

    private synchronized StoreRef getNextRef()
    {
        if (paused || (pauseCount > 0))
        {
            // System.out.println("Indexing suspended"+" id is "+this);
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
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException
    {
        @SuppressWarnings("unused")
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:alfresco/application-context.xml");
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        // Find bean implementaing SupportsBackgroundIndexing and register
        for(Object bgindexable : beanFactory.getBeansOfType(SupportsBackgroundIndexing.class).values())
        {
            if(bgindexable instanceof SupportsBackgroundIndexing)
            {
                ((SupportsBackgroundIndexing)bgindexable).setFullTextSearchIndexer(this);
            }
        }
        
    }
}
