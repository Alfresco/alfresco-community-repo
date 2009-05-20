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

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Component API for searching.  Delegates to the real {@link org.alfresco.service.cmr.search.SearchService searcher}
 * from the {@link #indexerAndSearcherFactory}.
 * 
 * Transactional support is free.
 * 
 * @author andyh
 * 
 */
public class SearcherComponent extends AbstractSearcherComponent
{
    private IndexerAndSearcher indexerAndSearcherFactory;
    
    public void setIndexerAndSearcherFactory(IndexerAndSearcher indexerAndSearcherFactory)
    {
        this.indexerAndSearcherFactory = indexerAndSearcherFactory;
    }

    public ResultSet query(StoreRef store,
            String language,
            String query,
            Path[] queryOptions,
            QueryParameterDefinition[] queryParameterDefinitions)
    {
        SearchService searcher = indexerAndSearcherFactory.getSearcher(store, true);
        return searcher.query(store, language, query, queryOptions, queryParameterDefinitions);
    }

    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ResultSet query(SearchParameters searchParameters)
    {
        if(searchParameters.getStores().size() != 1)
        {
            throw new IllegalStateException("Only one store can be searched at present");
        }
        StoreRef storeRef = searchParameters.getStores().get(0);
        SearchService searcher = indexerAndSearcherFactory.getSearcher(storeRef, !searchParameters.excludeDataInTheCurrentTransaction());
        return searcher.query(searchParameters);
    }

    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern) throws InvalidNodeRefException
    {
        return contains(nodeRef, propertyQName, googleLikePattern, SearchParameters.Operator.OR);
    }
    
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern, SearchParameters.Operator defaultOperator) throws InvalidNodeRefException
    {
        SearchService searcher = indexerAndSearcherFactory.getSearcher(nodeRef.getStoreRef(), true);
        return searcher.contains(nodeRef, propertyQName, googleLikePattern);
    }

    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS) throws InvalidNodeRefException
    {
        SearchService searcher = indexerAndSearcherFactory.getSearcher(nodeRef.getStoreRef(), true);
        return searcher.like(nodeRef, propertyQName, sqlLikePattern, includeFTS);
    }

    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        SearchService searcher = indexerAndSearcherFactory.getSearcher(contextNodeRef.getStoreRef(), true);
        return searcher.selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }

    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        SearchService searcher = indexerAndSearcherFactory.getSearcher(contextNodeRef.getStoreRef(), true);
        return searcher.selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }
}
