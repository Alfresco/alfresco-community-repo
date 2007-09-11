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
package org.alfresco.repo.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.search.impl.lucene.AVMLuceneIndexer;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Method interceptor for atomic indexing of AVM entries The proeprties can defined how stores are indexed based on type
 * (as set by Alfresco the Web site management UI) or based on the name of the store. Creates and deletes are indexed
 * synchronously. Updates may be asynchronous, synchronous or ignored by the index.
 * 
 * @author andyh
 */
public class AVMSnapShotTriggeredIndexingMethodInterceptor implements MethodInterceptor
{
    // Copy of store properties used to tag avm stores (a store propertry)

    public final static QName PROP_SANDBOX_STAGING_MAIN = QName.createQName(null, ".sandbox.staging.main");

    public final static QName PROP_SANDBOX_STAGING_PREVIEW = QName.createQName(null, ".sandbox.staging.preview");

    public final static QName PROP_SANDBOX_AUTHOR_MAIN = QName.createQName(null, ".sandbox.author.main");

    public final static QName PROP_SANDBOX_AUTHOR_PREVIEW = QName.createQName(null, ".sandbox.author.preview");

    public final static QName PROP_SANDBOX_WORKFLOW_MAIN = QName.createQName(null, ".sandbox.workflow.main");

    public final static QName PROP_SANDBOX_WORKFLOW_PREVIEW = QName.createQName(null, ".sandbox.workflow.preview");

    public final static QName PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN = QName.createQName(null, ".sandbox.author.workflow.main");

    public final static QName PROP_SANDBOX_AUTHOR_WORKFLOW_PREVIEW = QName.createQName(null, ".sandbox.author.workflow.preview");

    private AVMService avmService;

    private IndexerAndSearcher indexerAndSearcher;

    private boolean enableIndexing = true;

    private IndexMode defaultMode = IndexMode.ASYNCHRONOUS;

    private Map<String, IndexMode> modeCache = new HashMap<String, IndexMode>();

    private List<IndexingDefinition> indexingDefinitions = new ArrayList<IndexingDefinition>();

    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        if (enableIndexing)
        {
            if (mi.getMethod().getName().equals("createSnapshot"))
            {
                // May cause any number of other stores to do snap shot under the covers via layering or do nothing
                // So we have to watch what actually changes

                Object returnValue = mi.proceed();

                Map<String, Integer> snapShots = (Map<String, Integer>) returnValue;

                // Index any stores that have moved on
                for (String store : snapShots.keySet())
                {
                    int after = snapShots.get(store).intValue();
                    indexSnapshot(store, after);
                }
                return returnValue;
            }
            else if (mi.getMethod().getName().equals("purgeStore"))
            {
                String store = (String) mi.getArguments()[0];
                Object returnValue = mi.proceed();
                StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
                Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
                if (indexer instanceof AVMLuceneIndexer)
                {
                    AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                    avmIndexer.deleteIndex(store, IndexMode.SYNCHRONOUS);
                }
                return returnValue;
            }
            else if (mi.getMethod().getName().equals("createStore"))
            {
                String store = (String) mi.getArguments()[0];
                Object returnValue = mi.proceed();
                createIndex(store);
                return returnValue;
            }
            else if (mi.getMethod().getName().equals("renameStore"))
            {
                String from = (String) mi.getArguments()[0];
                String to = (String) mi.getArguments()[1];
                Object returnValue = mi.proceed();
                int after = avmService.getLatestSnapshotID(to);

                StoreRef fromRef = AVMNodeConverter.ToStoreRef(from);
                StoreRef toRef = AVMNodeConverter.ToStoreRef(to);

                Indexer indexer = indexerAndSearcher.getIndexer(fromRef);
                if (indexer instanceof AVMLuceneIndexer)
                {
                    AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                    avmIndexer.deleteIndex(from, IndexMode.SYNCHRONOUS);
                }

                indexer = indexerAndSearcher.getIndexer(toRef);
                if (indexer instanceof AVMLuceneIndexer)
                {
                    AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                    avmIndexer.createIndex(to, IndexMode.SYNCHRONOUS);
                    avmIndexer.index(to, 0, after, getIndexMode(to));
                }

                return returnValue;
            }
            else
            {
                return mi.proceed();
            }
        }
        else
        {
            return mi.proceed();
        }
    }

    /**
     * Set the AVM service
     * 
     * @param avmService
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /**
     * Set the AVM indexer and searcher
     * 
     * @param indexerAndSearcher
     */
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    /**
     * Enable or disable indexing
     * 
     * @param enableIndexing
     */
    public void setEnableIndexing(boolean enableIndexing)
    {
        this.enableIndexing = enableIndexing;
    }

    /**
     * Set the index modes.... Strings of the form ... (ASYNCHRONOUS | SYNCHRONOUS | UNINDEXED):(NAME | TYPE):regexp
     * 
     * @param definitions
     */
    public void setIndexingDefinitions(List<String> definitions)
    {
        indexingDefinitions.clear();
        for (String def : definitions)
        {
            IndexingDefinition id = new IndexingDefinition(def);
            indexingDefinitions.add(id);
        }
    }

    /**
     * Set the default index mode = used when there are no matches
     * 
     * @param defaultMode
     */
    public void setDefaultMode(IndexMode defaultMode)
    {
        this.defaultMode = defaultMode;
    }

    /**
     * @param store
     * @param before
     * @param after
     */
    public void indexSnapshot(String store, int before, int after)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            avmIndexer.index(store, before, after, getIndexMode(store));
        }
    }

    public void indexSnapshot(String store, int after)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            int before = avmIndexer.getLastIndexedSnapshot(store);
            avmIndexer.index(store, before, after, getIndexMode(store));
        }
    }

    /**
     * @param store
     * @return - the last indexed snapshot
     */
    public int getLastIndexedSnapshot(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            return avmIndexer.getLastIndexedSnapshot(store);
        }
        return -1;
    }

    /**
     * Is the snapshot applied to the index? Is there an entry for any node that was added OR have all the nodes in the
     * transaction been deleted as expected?
     * 
     * @param store
     * @param id
     * @return - true if applied, false if not
     */
    public boolean isSnapshotIndexed(String store, int id)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            return avmIndexer.isSnapshotIndexed(store, id);
        }
        return false;
    }

    /**
     * Check if the index is up to date according to its index defintion and that all asynchronous work is done.
     * 
     * @param store
     * @return
     */
    public boolean isIndexUpToDateAndSearchable(String store)
    {

        switch (getIndexMode(store))
        {
        case UNINDEXED:
            return false;
        case SYNCHRONOUS:
        case ASYNCHRONOUS:
            int last = avmService.getLatestSnapshotID(store);
            StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
            Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
            if (indexer instanceof AVMLuceneIndexer)
            {
                AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                avmIndexer.flushPending();
                return avmIndexer.isSnapshotSearchable(store, last);
            }
            return false;
        default:
            return false;
        }
    }
    
    /**
     * Check if the index is up to date according to its index defintion i it does not check that all asynchronous work is done.
     * 
     * @param store
     * @return
     */
    public boolean isIndexUpToDate(String store)
    {

        switch (getIndexMode(store))
        {
        case UNINDEXED:
            return true;
        case SYNCHRONOUS:
        case ASYNCHRONOUS:
            int last = avmService.getLatestSnapshotID(store);
            StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
            Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
            if (indexer instanceof AVMLuceneIndexer)
            {
                AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
                avmIndexer.flushPending();
                return avmIndexer.getLastIndexedSnapshot(store) == last;
            }
            return false;
        default:
            return false;
        }
    }

    /**
     * Given an avm store name determine if it is indexed and if so how.
     * 
     * @param store
     * @return
     */
    public synchronized IndexMode getIndexMode(String store)
    {
        IndexMode mode = modeCache.get(store);
        if (mode == null)
        {
            for (IndexingDefinition def : indexingDefinitions)
            {
                if (def.definitionType == DefinitionType.NAME)
                {
                    if (def.pattern.matcher(store).matches())
                    {
                        mode = def.indexMode;
                        modeCache.put(store, mode);
                        break;
                    }
                }
                else
                {
                    String storeType = getStoreType(store).toString();
                    if (def.pattern.matcher(storeType).matches())
                    {
                        mode = def.indexMode;
                        modeCache.put(store, mode);
                        break;
                    }

                }
            }
        }
        // No definition
        if (mode == null)
        {
            mode = defaultMode;
            modeCache.put(store, mode);
        }
        return mode;
    }

    private class IndexingDefinition
    {
        IndexMode indexMode;

        DefinitionType definitionType;

        Pattern pattern;

        IndexingDefinition(String definition)
        {
            String[] split = definition.split(":", 3);
            if (split.length != 3)
            {
                throw new AlfrescoRuntimeException("Invalid index defintion. Must be of of the form IndexMode:DefinitionType:regular expression");
            }
            indexMode = IndexMode.valueOf(split[0].toUpperCase());
            definitionType = DefinitionType.valueOf(split[1].toUpperCase());
            pattern = Pattern.compile(split[2]);
        }
    }

    private StoreType getStoreType(String name)
    {
        if (avmService.getStore(name) != null)
        {
            Map<QName, PropertyValue> storeProperties = avmService.getStoreProperties(name);
            if (storeProperties.containsKey(PROP_SANDBOX_STAGING_MAIN))
            {
                return StoreType.STAGING;
            }
            else if (storeProperties.containsKey(PROP_SANDBOX_STAGING_PREVIEW))
            {
                return StoreType.STAGING_PREVIEW;
            }
            else if (storeProperties.containsKey(PROP_SANDBOX_AUTHOR_MAIN))
            {
                return StoreType.AUTHOR;
            }
            else if (storeProperties.containsKey(PROP_SANDBOX_AUTHOR_PREVIEW))
            {
                return StoreType.AUTHOR_PREVIEW;
            }
            else if (storeProperties.containsKey(PROP_SANDBOX_WORKFLOW_MAIN))
            {
                return StoreType.WORKFLOW;
            }
            else if (storeProperties.containsKey(PROP_SANDBOX_WORKFLOW_PREVIEW))
            {
                return StoreType.WORKFLOW_PREVIEW;
            }
            else if (storeProperties.containsKey(PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN))
            {
                return StoreType.AUTHOR_WORKFLOW;
            }
            else if (storeProperties.containsKey(PROP_SANDBOX_AUTHOR_WORKFLOW_PREVIEW))
            {
                return StoreType.AUTHOR_WORKFLOW_PREVIEW;
            }
            else
            {
                return StoreType.UNKNOWN;
            }
        }
        else
        {
            return StoreType.UNKNOWN;
        }
    }

    private enum DefinitionType
    {
        NAME, TYPE;
    }

    private enum StoreType
    {
        STAGING, STAGING_PREVIEW, AUTHOR, AUTHOR_PREVIEW, WORKFLOW, WORKFLOW_PREVIEW, AUTHOR_WORKFLOW, AUTHOR_WORKFLOW_PREVIEW, UNKNOWN;
    }

    public boolean hasIndexBeenCreated(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            avmIndexer.flushPending();
            return avmIndexer.hasIndexBeenCreated(store);
        }
        return false;
    }

    public void createIndex(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            avmIndexer.createIndex(store, IndexMode.SYNCHRONOUS);
        }
    }
}
