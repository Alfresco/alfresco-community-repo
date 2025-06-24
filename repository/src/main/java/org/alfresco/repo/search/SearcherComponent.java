/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.search;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
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
 * Component API for searching. Delegates to the real {@link org.alfresco.service.cmr.search.SearchService searcher} from the {@link #indexerAndSearcherFactory}.
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

    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ResultSet query(StoreRef store,
            String language,
            String query,
            QueryParameterDefinition[] queryParameterDefinitions)
    {
        SearchService searcher = indexerAndSearcherFactory.getSearcher(store, true);
        return searcher.query(store, language, query, queryParameterDefinitions);
    }

    public ResultSet query(SearchParameters searchParameters)
    {
        if (searchParameters.getStores().size() == 0)
        {
            throw new IllegalStateException("At least one store must be defined to search");
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
