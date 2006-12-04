/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search.impl.lucene.fts;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.repo.search.IndexerSPI;
import org.alfresco.repo.search.impl.lucene.LuceneIndexerAndSearcher;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FullTextSearchIndexerImpl implements FTSIndexerAware, FullTextSearchIndexer
{
    private enum State
    {
        ACTIVE, PAUSING, PAUSED
    };

    private static Set<StoreRef> requiresIndex = new LinkedHashSet<StoreRef>();

    private static Set<StoreRef> indexing = new HashSet<StoreRef>();

    LuceneIndexerAndSearcher luceneIndexerAndSearcherFactory;

    private int pauseCount = 0;

    private boolean paused = false;

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
                IndexerSPI indexer = luceneIndexerAndSearcherFactory.getIndexer(toIndex);
                indexer.registerCallBack(this);
                done += indexer.updateFullTextSearch(1000);
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

    public void setLuceneIndexerAndSearcherFactory(LuceneIndexerAndSearcher luceneIndexerAndSearcherFactory)
    {
        this.luceneIndexerAndSearcherFactory = luceneIndexerAndSearcherFactory;
    }

    public static void main(String[] args) throws InterruptedException
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:alfresco/application-context.xml");
    }
}
