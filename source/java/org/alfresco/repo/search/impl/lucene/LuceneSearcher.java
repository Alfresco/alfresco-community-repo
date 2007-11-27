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

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.util.Pair;

/**
 * Lucene implementation specific entension to the seracher API
 * @author andyh
 *
 */
public interface LuceneSearcher extends SearchService
{
    /**
     * Check if the index exists 
     * @return - true if it exists
     */
   public boolean indexExists();
   /**
    * Ste the node service
    * @param nodeService
    */
   public void setNodeService(NodeService nodeService);
   /**
    * Set the name space service
    * @param namespacePrefixResolver
    */
   public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver);
   
   /**
    * Get top terms
    * 
    * @param field
    * @param count
    * @return
    */
   public List<Pair<String, Integer>> getTopTerms(String field, int count);
}
