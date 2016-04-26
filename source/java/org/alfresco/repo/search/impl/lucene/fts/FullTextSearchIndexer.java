package org.alfresco.repo.search.impl.lucene.fts;

import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * API for full text search indexing in the background
 * 
 * @author andyh
 */
public interface FullTextSearchIndexer extends BeanFactoryAware
{
    /**
     * Mark a store as dirty, requiring a background index update to fix it up.
     * 
     * @param storeRef StoreRef
     */
    public abstract void requiresIndex(StoreRef storeRef);

    /**
     * Call back to report state back to the indexer
     * 
     * @param storeRef StoreRef
     * @param remaining int
     * @param t Throwable
     */
    public abstract void indexCompleted(StoreRef storeRef, int remaining, Throwable t);

    /**
     * Pause indexing 9no back ground indexing until a resume is called)
     * @throws InterruptedException
     */
    public abstract void pause() throws InterruptedException;

    /**
     * Resume after a pause
     * 
     * @throws InterruptedException
     */
    public abstract void resume() throws InterruptedException;

    /**
     * Do a chunk of outstanding indexing work
     *
     */
    public abstract void index();

}