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
