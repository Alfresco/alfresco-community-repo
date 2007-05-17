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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Factory for AVM indexers and searchers
 * 
 * @author andyh
 *
 */
public class AVMLuceneIndexerAndSearcherFactory extends AbstractLuceneIndexerAndSearcherFactory
{
    private DictionaryService dictionaryService;

    private NamespaceService nameSpaceService;

    private ContentService contentService;

    private AVMService avmService;

    private AVMSyncService avmSyncService;

    private NodeService nodeService;

    private ContentStore contentStore;

    /**
     * Set the dictionary service
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
     * Set the content service
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Set the AVM service
     * @param avmService
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /**
     * Set the AVM sync service
     * @param avmSyncService
     */
    public void setAvmSyncService(AVMSyncService avmSyncService)
    {
        this.avmSyncService = avmSyncService;
    }

    /**
     * Set the node service
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    
    
    /**
     * Set the content service
     * @param contentStore
     */
    public void setContentStore(ContentStore contentStore)
    {
        this.contentStore = contentStore;
    }

    @Override
    protected LuceneIndexer createIndexer(StoreRef storeRef, String deltaId)
    {
        AVMLuceneIndexerImpl indexer = AVMLuceneIndexerImpl.getUpdateIndexer(storeRef, deltaId, this);
        indexer.setDictionaryService(dictionaryService);
        indexer.setContentService(contentService);
        indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());
        indexer.setAvmService(avmService);
        indexer.setAvmSyncService(avmSyncService);
        indexer.setContentStore(contentStore);
        return indexer;
    }

    @Override
    protected List<StoreRef> getAllStores()
    {
        List<AVMStoreDescriptor> stores = avmService.getStores();
        List<StoreRef> storeRefs = new ArrayList<StoreRef>(stores.size());
        for(AVMStoreDescriptor storeDesc : stores)
        {
            StoreRef storeRef = AVMNodeConverter.ToStoreRef(storeDesc.getName());
            storeRefs.add(storeRef);
        }
        return storeRefs;
    }

    @Override
    protected LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer) throws SearcherException
    {
        //TODO: Store overlays
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(storeRef, indexer, this);
        searcher.setNamespacePrefixResolver(nameSpaceService);
        // searcher.setLuceneIndexLock(luceneIndexLock);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setQueryRegister(getQueryRegister());
        return searcher;
    }

}
