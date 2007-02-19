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
    public abstract IndexerSPI getIndexer(StoreRef storeRef) throws IndexerException;

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
