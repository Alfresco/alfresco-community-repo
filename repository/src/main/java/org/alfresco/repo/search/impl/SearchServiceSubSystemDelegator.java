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
package org.alfresco.repo.search.impl;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.SearchLanguageConversion;

/**
 * Send calls that have to use the sub-system to the delegate. selectNodes and selectProperties will avoid the sub-system if possible.
 * 
 * @author Andy
 *
 */
public class SearchServiceSubSystemDelegator implements SearchService
{

    private SearchService subSystem;

    private NodeService nodeService;

    private DictionaryService dictionaryService;

    /**
     * @param subSystem
     *            the subSystem to set
     */
    public void setSubSystem(SearchService subSystem)
    {
        this.subSystem = subSystem;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param dictionaryService
     *            the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param store
     *            StoreRef
     * @param language
     *            String
     * @param query
     *            String
     * @return ResultSet
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef, java.lang.String, java.lang.String)
     */
    public ResultSet query(StoreRef store, String language, String query)
    {
        return subSystem.query(store, language, query);
    }

    /**
     * @param store
     *            StoreRef
     * @param language
     *            String
     * @param query
     *            String
     * @param queryParameterDefinitions
     *            QueryParameterDefinition[]
     * @return ResultSet
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef, java.lang.String, java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[])
     */
    public ResultSet query(StoreRef store, String language, String query, QueryParameterDefinition[] queryParameterDefinitions)
    {
        return subSystem.query(store, language, query, queryParameterDefinitions);
    }

    /**
     * @param store
     *            StoreRef
     * @param queryId
     *            QName
     * @param queryParameters
     *            QueryParameter[]
     * @return ResultSet
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.search.QueryParameter[])
     */
    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        return subSystem.query(store, queryId, queryParameters);
    }

    /**
     * @param searchParameters
     *            SearchParameters
     * @return ResultSet
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.search.SearchParameters)
     */
    public ResultSet query(SearchParameters searchParameters)
    {
        return subSystem.query(searchParameters);
    }

    /**
     * @param contextNodeRef
     *            NodeRef
     * @param xpath
     *            String
     * @param parameters
     *            QueryParameterDefinition[]
     * @param namespacePrefixResolver
     *            NamespacePrefixResolver
     * @param followAllParentLinks
     *            boolean
     * @throws InvalidNodeRefException
     * @throws XPathException
     * @see org.alfresco.service.cmr.search.SearchService#selectNodes(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[], org.alfresco.service.namespace.NamespacePrefixResolver, boolean)
     */
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, SearchService.LANGUAGE_XPATH);
    }

    /**
     * @param contextNodeRef
     *            NodeRef
     * @param xpath
     *            String
     * @param parameters
     *            QueryParameterDefinition[]
     * @param namespacePrefixResolver
     *            NamespacePrefixResolver
     * @param followAllParentLinks
     *            boolean
     * @param language
     *            String
     * @throws InvalidNodeRefException
     * @throws XPathException
     * @see org.alfresco.service.cmr.search.SearchService#selectNodes(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[], org.alfresco.service.namespace.NamespacePrefixResolver, boolean, java.lang.String)
     */
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, dictionaryService, this);
        return nodeSearcher.selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }

    /**
     * @param contextNodeRef
     *            NodeRef
     * @param xpath
     *            String
     * @param parameters
     *            QueryParameterDefinition[]
     * @param namespacePrefixResolver
     *            NamespacePrefixResolver
     * @param followAllParentLinks
     *            boolean
     * @throws InvalidNodeRefException
     * @throws XPathException
     * @see org.alfresco.service.cmr.search.SearchService#selectProperties(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[], org.alfresco.service.namespace.NamespacePrefixResolver, boolean)
     */
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, SearchService.LANGUAGE_XPATH);
    }

    /**
     * @param contextNodeRef
     *            NodeRef
     * @param xpath
     *            String
     * @param parameters
     *            QueryParameterDefinition[]
     * @param namespacePrefixResolver
     *            NamespacePrefixResolver
     * @param followAllParentLinks
     *            boolean
     * @param language
     *            String
     * @throws InvalidNodeRefException
     * @throws XPathException
     * @see org.alfresco.service.cmr.search.SearchService#selectProperties(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[], org.alfresco.service.namespace.NamespacePrefixResolver, boolean, java.lang.String)
     */
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, dictionaryService, this);
        return nodeSearcher.selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }

    /**
     * @param nodeRef
     *            NodeRef
     * @param propertyQName
     *            QName
     * @param googleLikePattern
     *            String
     * @return boolean
     * @throws InvalidNodeRefException
     * @see org.alfresco.service.cmr.search.SearchService#contains(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String)
     */
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern) throws InvalidNodeRefException
    {
        return subSystem.contains(nodeRef, propertyQName, googleLikePattern);
    }

    /**
     * @param nodeRef
     *            NodeRef
     * @param propertyQName
     *            QName
     * @param googleLikePattern
     *            String
     * @param defaultOperator
     *            Operator
     * @return boolean
     * @throws InvalidNodeRefException
     * @see org.alfresco.service.cmr.search.SearchService#contains(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.cmr.search.SearchParameters.Operator)
     */
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern, Operator defaultOperator) throws InvalidNodeRefException
    {
        return subSystem.contains(nodeRef, propertyQName, googleLikePattern, defaultOperator);
    }

    /**
     * @param nodeRef
     *            NodeRef
     * @param propertyQName
     *            QName
     * @param sqlLikePattern
     *            String
     * @param includeFTS
     *            boolean
     * @return boolean
     * @throws InvalidNodeRefException
     * @see org.alfresco.service.cmr.search.SearchService#like(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, boolean)
     */
    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS) throws InvalidNodeRefException
    {
        if (propertyQName == null)
        {
            throw new IllegalArgumentException("Property QName is mandatory for the like expression");
        }

        if (includeFTS)
        {
            return subSystem.like(nodeRef, propertyQName, sqlLikePattern, includeFTS);
        }
        else
        {
            // convert the SQL-like pattern into a Lucene-compatible string
            String pattern = SearchLanguageConversion.convertXPathLikeToRegex(sqlLikePattern.toLowerCase());

            Serializable property = nodeService.getProperty(nodeRef, propertyQName);
            if (property == null)
            {
                return false;
            }
            else
            {
                String propertyString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, propertyQName));
                return propertyString.toLowerCase().matches(pattern);
            }
        }

    }

}
