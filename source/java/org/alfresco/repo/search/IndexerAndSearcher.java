/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search;

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
     * @param storeRef
     * @return
     * @throws IndexerException
     */
    public abstract Indexer getIndexer(StoreRef storeRef) throws IndexerException;

    /**
     * Get a searcher for a store
     * 
     * @param storeRef
     * @param searchDelta -
     *            serach the in progress transaction as well as the main index
     *            (this is ignored for searches that do full text)
     * @return
     * @throws SearcherException
     */
    public abstract SearchService getSearcher(StoreRef storeRef, boolean searchDelta) throws SearcherException;
    
    
    /**
     * Do any indexing that may be pending on behalf of the current transaction.
     *
     */
    public abstract void flush();
}
