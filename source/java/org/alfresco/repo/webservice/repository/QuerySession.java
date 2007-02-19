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
package org.alfresco.repo.webservice.repository;

import java.io.Serializable;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Interface definition for a QuerySession.
 * 
 * @author gavinc
 */
public interface QuerySession extends Serializable
{
    /** System column namess */
    public static String SYS_COL_ASSOC_TYPE = "associationType";
    public static String SYS_COL_ASSOC_NAME = "associationName";
    public static String SYS_COL_IS_PRIMARY = "isPrimary";
    public static String SYS_COL_NTH_SIBLING = "nthSibling";
    
   /**
    * Retrieves the id this query session can be identified as
    * 
    * @return Id of this query session
    */
   public String getId();
   
   /**
    * Returns a QueryResult object representing the next batch of results.
    * QueryResult will contain a maximum of items as determined by the 
    * <code>fetchSize</code> element of the QueryConfiguration SOAP header.
    * 
    * When the last batch of results is being returned the querySession of
    * QueryResult will be null.
    * 
    * @see org.alfresco.repo.webservice.repository.QuerySession#getId()
    * @param searchService The SearchService to use for gathering the results
    * @param nodeService The NodeService to use for gathering the results
    * @param namespaceService The NamespaceService to use
    * @return QueryResult containing the next batch of results or null if there
    * are no more results
    */
   public QueryResult getNextResultsBatch(
           SearchService searchService, 
           NodeService nodeService, 
           NamespaceService namespaceService,
           DictionaryService dictionaryService);
}
