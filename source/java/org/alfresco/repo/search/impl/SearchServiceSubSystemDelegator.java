/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
 * Send calls that have to use the sub-system to the delegate.
 * selectNodes and selectProperties will avoid the sub-system if possible.  
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
     * @param subSystem the subSystem to set
     */
    public void setSubSystem(SearchService subSystem)
    {
        this.subSystem = subSystem;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param store
     * @param language
     * @param query
     * @return
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef, java.lang.String, java.lang.String)
     */
    public ResultSet query(StoreRef store, String language, String query)
    {
        return subSystem.query(store, language, query);
    }

    /**
     * @param store
     * @param language
     * @param query
     * @param queryParameterDefinitions
     * @return
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef, java.lang.String, java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[])
     */
    public ResultSet query(StoreRef store, String language, String query, QueryParameterDefinition[] queryParameterDefinitions)
    {
        return subSystem.query(store, language, query, queryParameterDefinitions);
    }

    /**
     * @param store
     * @param queryId
     * @param queryParameters
     * @return
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.search.QueryParameter[])
     */
    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        return subSystem.query(store, queryId, queryParameters);
    }

    /**
     * @param searchParameters
     * @return
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.search.SearchParameters)
     */
    public ResultSet query(SearchParameters searchParameters)
    {
        return subSystem.query(searchParameters);
    }

    /**
     * @param contextNodeRef
     * @param xpath
     * @param parameters
     * @param namespacePrefixResolver
     * @param followAllParentLinks
     * @return
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
     * @param xpath
     * @param parameters
     * @param namespacePrefixResolver
     * @param followAllParentLinks
     * @param language
     * @return
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
     * @param xpath
     * @param parameters
     * @param namespacePrefixResolver
     * @param followAllParentLinks
     * @return
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
     * @param xpath
     * @param parameters
     * @param namespacePrefixResolver
     * @param followAllParentLinks
     * @param language
     * @return
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
     * @param propertyQName
     * @param googleLikePattern
     * @return
     * @throws InvalidNodeRefException
     * @see org.alfresco.service.cmr.search.SearchService#contains(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String)
     */
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern) throws InvalidNodeRefException
    {
        return subSystem.contains(nodeRef, propertyQName, googleLikePattern);
    }

    /**
     * @param nodeRef
     * @param propertyQName
     * @param googleLikePattern
     * @param defaultOperator
     * @return
     * @throws InvalidNodeRefException
     * @see org.alfresco.service.cmr.search.SearchService#contains(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.cmr.search.SearchParameters.Operator)
     */
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern, Operator defaultOperator) throws InvalidNodeRefException
    {
        return subSystem.contains(nodeRef, propertyQName, googleLikePattern, defaultOperator);
    }

    /**
     * @param nodeRef
     * @param propertyQName
     * @param sqlLikePattern
     * @param includeFTS
     * @return
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
