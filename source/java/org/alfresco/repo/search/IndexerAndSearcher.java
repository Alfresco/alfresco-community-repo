package org.alfresco.repo.search;

import java.util.Map;

import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Interface for Indexer and Searcher Factories to implement
 * 
 * @author andyh
 * 
 */
public interface IndexerAndSearcher
{
    /**
     * Get an indexer for a store
     * 
     * @param storeRef StoreRef
     * @return Indexer
     * @throws IndexerException
     */
    public abstract Indexer getIndexer(StoreRef storeRef) throws IndexerException;

    /**
     * Get a searcher for a store
     * 
     * @param storeRef StoreRef
     * @param searchDelta -
     *            serach the in progress transaction as well as the main index
     *            (this is ignored for searches that do full text)
     * @return SearchService
     * @throws SearcherException
     */
    public abstract SearchService getSearcher(StoreRef storeRef, boolean searchDelta) throws SearcherException;
    
    
    /**
     * Do any indexing that may be pending on behalf of the current transaction.
     *
     */
    public abstract void flush();

    /**
     * @param luceneQueryLanguageSPI LuceneQueryLanguageSPI
     */
    public abstract void registerQueryLanguage(LuceneQueryLanguageSPI luceneQueryLanguageSPI);

    /**
     * @return Map
     */
    public abstract Map<String, LuceneQueryLanguageSPI> getQueryLanguages();
}
