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
package org.alfresco.repo.search.impl.lucene;

import java.util.List;

import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.SupportsBackgroundIndexing;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Factory for ADM indxers and searchers
 * @author andyh
 *
 */
public class ADMLuceneIndexerAndSearcherFactory extends AbstractLuceneIndexerAndSearcherFactory implements SupportsBackgroundIndexing
{
    private DictionaryService dictionaryService;

    private NamespaceService nameSpaceService;

    private NodeService nodeService;

    private FullTextSearchIndexer fullTextSearchIndexer;

    private ContentService contentService;

    /**
     * Set the dictinary service
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the name space service
     * @param nameSpaceService
     */
    public void setNameSpaceService(NamespaceService nameSpaceService)
    {
        this.nameSpaceService = nameSpaceService;
    }

    /**
     * Set the node service
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFullTextSearchIndexer(FullTextSearchIndexer fullTextSearchIndexer)
    {
        this.fullTextSearchIndexer = fullTextSearchIndexer;
    }

    /**
     * Set the content service
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    protected LuceneIndexer createIndexer(StoreRef storeRef, String deltaId)
    {
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(storeRef, deltaId, this);
        indexer.setNodeService(nodeService);
        indexer.setDictionaryService(dictionaryService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setFullTextSearchIndexer(fullTextSearchIndexer);
        indexer.setContentService(contentService);
        indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());
        return indexer;
    }

    protected LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer) throws SearcherException
    {
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(storeRef, indexer, this);
        searcher.setNamespacePrefixResolver(nameSpaceService);
        // searcher.setLuceneIndexLock(luceneIndexLock);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setQueryRegister(getQueryRegister());
        return searcher;
    }

   
    protected List<StoreRef> getAllStores()
    {
        return nodeService.getStores();
    }
}
