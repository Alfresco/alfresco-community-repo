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
}
