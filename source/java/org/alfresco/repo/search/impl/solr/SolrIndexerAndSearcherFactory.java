package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.NoActionIndexer;
import org.alfresco.repo.search.impl.lucene.AbstractIndexerAndSearcher;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

/**
 * @author Andy
 *
 */
public class SolrIndexerAndSearcherFactory extends AbstractIndexerAndSearcher
{

    private DictionaryService dictionaryService;
    private NamespacePrefixResolver namespacePrefixResolver;
    private NodeService nodeService;
    private QueryRegisterComponent queryRegister;
    
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return namespacePrefixResolver;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public QueryRegisterComponent getQueryRegister()
    {
        return queryRegister;
    }

    public void setQueryRegister(QueryRegisterComponent queryRegister)
    {
        this.queryRegister = queryRegister;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.search.IndexerAndSearcher#getIndexer(org.alfresco.service.cmr.repository.StoreRef)
     */
    @Override
    public Indexer getIndexer(StoreRef storeRef) throws IndexerException
    {
        return new NoActionIndexer();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.IndexerAndSearcher#getSearcher(org.alfresco.service.cmr.repository.StoreRef, boolean)
     */
    @Override
    public SearchService getSearcher(StoreRef storeRef, boolean searchDelta) throws SearcherException
    {
         SolrSearchService searchService = new SolrSearchService();
         searchService.setDictionaryService(dictionaryService);
         searchService.setNamespacePrefixResolver(namespacePrefixResolver);
         searchService.setNodeService(nodeService);
         searchService.setQueryLanguages(getQueryLanguages());
         searchService.setQueryRegister(queryRegister);
         return searchService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.IndexerAndSearcher#flush()
     */
    @Override
    public void flush()
    {
        // Nothing to do
    }

}
