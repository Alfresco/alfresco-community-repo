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
package org.alfresco.repo.search.impl.noindex;

import java.io.Serializable;
import java.util.List;

import org.alfresco.error.StackTraceUtil;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andy
 * log4j:logger=org.alfresco.repo.search.impl.noindex.NoIndexSearchService
 */
public class NoIndexSearchService implements SearchService
{
    private static Log s_logger = LogFactory.getLog(NoIndexSearchService.class);

    private NodeService nodeService;

    private DictionaryService dictionaryService;

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef,
     * java.lang.String, java.lang.String)
     */
    @Override
    public ResultSet query(StoreRef store, String language, String query)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("query   store = " + store + "   language = " + language + "   query = " + query);
        }
        trace();
        return new EmptyResultSet();
    }

    private void trace()
    {
        if (s_logger.isTraceEnabled())
        {
            Exception e = new Exception();
            e.fillInStackTrace();

            StringBuilder sb = new StringBuilder(1024);
            StackTraceUtil.buildStackTrace("Search trace ...", e.getStackTrace(), sb, -1);
            s_logger.trace(sb);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef,
     * java.lang.String, java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[])
     */
    @Override
    public ResultSet query(StoreRef store, String language, String query, QueryParameterDefinition[] queryParameterDefinitions)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("query   store = " + store + "   language = " + language + "   query = " + query + "   queryParameterDefinitions = " + queryParameterDefinitions);
        }
        trace();
        return new EmptyResultSet();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef,
     * org.alfresco.service.namespace.QName, org.alfresco.service.cmr.search.QueryParameter[])
     */
    @Override
    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("query   store = " + store + "   queryId = " + queryId + "   queryParameters = " + queryParameters);
        }
        trace();
        return new EmptyResultSet();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.search.SearchParameters)
     */
    @Override
    public ResultSet query(SearchParameters searchParameters)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("query   searchParameters = " + searchParameters);
        }
        trace();
        return new EmptyResultSet();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#selectNodes(org.alfresco.service.cmr.repository.NodeRef,
     * java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[],
     * org.alfresco.service.namespace.NamespacePrefixResolver, boolean)
     */
    @Override
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, SearchService.LANGUAGE_XPATH);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#selectNodes(org.alfresco.service.cmr.repository.NodeRef,
     * java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[],
     * org.alfresco.service.namespace.NamespacePrefixResolver, boolean, java.lang.String)
     */
    @Override
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, dictionaryService, this);
        return nodeSearcher.selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#selectProperties(org.alfresco.service.cmr.repository.NodeRef,
     * java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[],
     * org.alfresco.service.namespace.NamespacePrefixResolver, boolean)
     */
    @Override
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, SearchService.LANGUAGE_XPATH);

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#selectProperties(org.alfresco.service.cmr.repository.NodeRef,
     * java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[],
     * org.alfresco.service.namespace.NamespacePrefixResolver, boolean, java.lang.String)
     */
    @Override
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, dictionaryService, this);
        return nodeSearcher.selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#contains(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.namespace.QName, java.lang.String)
     */
    @Override
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern) throws InvalidNodeRefException
    {
        return contains(nodeRef, propertyQName, googleLikePattern, SearchParameters.Operator.OR);

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#contains(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.namespace.QName, java.lang.String,
     * org.alfresco.service.cmr.search.SearchParameters.Operator)
     */
    @Override
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern, Operator defaultOperator) throws InvalidNodeRefException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("contains   nodeRef = "
                    + nodeRef + "   propertyQName = " + propertyQName + "   googleLikePattern = " + googleLikePattern + "   defaultOperator = " + defaultOperator);
        }
        trace();
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#like(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.namespace.QName, java.lang.String, boolean)
     */
    @Override
    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS) throws InvalidNodeRefException
    {
        // only inlcude FTS depends on the index ...
        if (includeFTS)
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("contains   nodeRef = "
                        + nodeRef + "   propertyQName = " + propertyQName + "   sqlLikePattern = " + sqlLikePattern + "   includeFTS = " + includeFTS);
            }
            trace();
        }
        return false;
    }

}
