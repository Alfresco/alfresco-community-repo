package org.alfresco.repo.search;

import org.alfresco.repo.search.impl.lucene.fts.FTSIndexerAware;

/**
 * Add support for FTS indexing
 * 
 * @author andyh
 */
public interface BackgroundIndexerAware extends SupportsBackgroundIndexing
{
    /**
     * Register call back handler when the indexing chunk is done
     * 
     * @param callBack FTSIndexerAware
     */
    public void registerCallBack(FTSIndexerAware callBack);

    /**
     * Peform a chunk of background FTS (and other non atomic property) indexing
     * 
     * @param i int
     * @return - the number of docs updates
     */
    public int updateFullTextSearch(int i);

    
}
