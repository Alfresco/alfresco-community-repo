package org.alfresco.repo.search.impl.noindex;

import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.solr.SolrIndexerAndSearcherFactory;
import org.alfresco.repo.search.impl.solr.SolrSearchService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;

/**
 * @author Andy
 *
 */
public class NoIndexIndexerAndSearcherFactory extends SolrIndexerAndSearcherFactory
{

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.IndexerAndSearcher#getSearcher(org.alfresco.service.cmr.repository.StoreRef, boolean)
     */
    @Override
    public SearchService getSearcher(StoreRef storeRef, boolean searchDelta) throws SearcherException
    {
         NoIndexSearchService searchService = new NoIndexSearchService();
         searchService.setDictionaryService(getDictionaryService());
         searchService.setNamespacePrefixResolver(getNamespacePrefixResolver());
         searchService.setNodeService(getNodeService());
         searchService.setQueryLanguages(getQueryLanguages());
         searchService.setQueryRegister(getQueryRegister());
         return searchService;
    }
}
