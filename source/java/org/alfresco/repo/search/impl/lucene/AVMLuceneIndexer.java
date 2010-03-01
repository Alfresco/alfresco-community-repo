/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.search.impl.lucene;

import org.alfresco.repo.search.BackgroundIndexerAware;
import org.alfresco.repo.search.IndexMode;

/**
 * AVM specific indxer support
 * 
 * @author andyh
 *
 */
public interface AVMLuceneIndexer extends LuceneIndexer, BackgroundIndexerAware
{
    /**
     * Index a specified change to a store between two snapshots 
     * 
     * @param store - the name of the store
     * @param srcVersion - the id of the snapshot before the changeset
     * @param dstVersion - the id of the snapshot created by the change set
     * @param mode 
     */
    public void index(String store, int srcVersion, int dstVersion, IndexMode mode);
    
    /**
     * Delete the index for the specified store.
     * 
     * @param store
     * @param mode 
     */
    public void deleteIndex(String store, IndexMode mode);
    
    /**
     * Create an index for the specified store.
     * This makes sure that the root node for the store is indexed correctly.
     * 
     * @param store
     * @param mode
     */
    public void createIndex(String store, IndexMode mode);
    
    /**
     * Get the id of the last snapshot added to the index
     * @param store 
     * 
     * @param mode 
     *    - IndexMode.SYNCHRONOUS - the last searchable snapshop
     *    - IndexMode.ASYNCHRONOUS - the last pending snapshot to be indexed. It may or may not be searchable.
     * @return - the snapshot id
     */
    public int getLastIndexedSnapshot(String store);
    
    /**
     * Is the snapshot applied to the index?
     *      
     * Is there an entry for any node that was added OR have all the nodes in the transaction been deleted as expected?
     *      
     * @param store
     * @param id
     * @return - true if applied, false if not
     */
    public boolean isSnapshotIndexed(String store, int id);
    
    /**
     * Is snapshot searchable
     * @param store 
     * @param id 
     * @return - true if snapshot has been fully indexed, false if pending or unindexed.
     */
    public boolean isSnapshotSearchable(String store, int id);

    /**
     * Has the index been ceated
     * 
     * @param store
     * @return
     */
    public boolean hasIndexBeenCreated(String store);
    
    /**
     * Get the number of docs this indexer has indexed so far
     * @return
     */
    public long getIndexedDocCount();
}
