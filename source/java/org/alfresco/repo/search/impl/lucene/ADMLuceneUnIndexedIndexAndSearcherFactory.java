package org.alfresco.repo.search.impl.lucene;

import org.alfresco.service.cmr.repository.StoreRef;

public class ADMLuceneUnIndexedIndexAndSearcherFactory extends ADMLuceneIndexerAndSearcherFactory
{

    @Override
    protected LuceneIndexer createIndexer(StoreRef storeRef, String deltaId)
    {
        ADMLuceneNoActionIndexerImpl indexer = ADMLuceneIndexerImpl.getNoActionIndexer(storeRef, deltaId, this);
        indexer.setNodeService(nodeService);
        indexer.setBulkLoader(getBulkLoader());
        indexer.setTenantService(tenantService);
        indexer.setDictionaryService(dictionaryService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setFullTextSearchIndexer(fullTextSearchIndexer);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);
        indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());
        indexer.setTypeIndexFilter(typeIndexFilter);
        indexer.setAspectIndexFilter(aspectIndexFilter);
        return indexer;
    }
}
