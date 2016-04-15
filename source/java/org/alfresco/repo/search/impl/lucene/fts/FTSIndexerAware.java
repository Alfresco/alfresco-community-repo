package org.alfresco.repo.search.impl.lucene.fts;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Supports  callback to the FTS indexer to report what has been done
 * @author andyh
 *
 */
public interface FTSIndexerAware
{
    /**
     * Call back used by the background indexer 
     * 
     * @param storeRef StoreRef
     * @param remaining int
     * @param t Throwable
     */
    public void indexCompleted(StoreRef storeRef, int remaining, Throwable t);   
}
