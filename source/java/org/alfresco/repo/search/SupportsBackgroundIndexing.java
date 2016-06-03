package org.alfresco.repo.search;

import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;

/**
 * Interface to support backgournd indexing updates
 * 
 * @author andyh
 *
 */
public interface SupportsBackgroundIndexing
{
    /**
     * Set the back ground indexer manager
     * 
     * @param fullTextSearchIndexer FullTextSearchIndexer
     */
    public void setFullTextSearchIndexer(FullTextSearchIndexer fullTextSearchIndexer);
}
