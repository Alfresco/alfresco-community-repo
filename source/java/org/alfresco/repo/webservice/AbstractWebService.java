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
package org.alfresco.repo.webservice;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Abstract base class for all web service implementations, provides support for common service injection
 * 
 * @author gavinc
 */
public abstract class AbstractWebService
{
    protected ServiceRegistry serviceRegistry;
    protected DictionaryService dictionaryService;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected SearchService searchService;
    protected NamespaceService namespaceService;

    protected SimpleCache<String, QuerySession> querySessionCache;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Sets the instance of the DictionaryService to be used
     * 
     * @param dictionaryService
     *            The DictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Sets the instance of the NodeService to be used
     * 
     * @param nodeService The NodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the ContentService instance to use
     * 
     * @param contentSvc The ContentService
     */
    public void setContentService(ContentService contentSvc)
    {
        this.contentService = contentSvc;
    }

    /**
     * Sets the instance of the SearchService to be used
     * 
     * @param searchService The SearchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Sets the instance of the NamespaceService to be used
     * 
     * @param namespaceService The NamespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Sets the cache to use for storing the the query session's paging information by query session ID.
     * 
     * @param querySessionCache         the cache.  Cluster replication should be via serialization of
     *                                  the cache values.
     */
    public void setQuerySessionCache(SimpleCache<String, QuerySession> querySessionCache)
    {
        this.querySessionCache = querySessionCache;
    }
}
