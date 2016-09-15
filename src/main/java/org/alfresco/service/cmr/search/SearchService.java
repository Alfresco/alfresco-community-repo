/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.search;

import java.io.Serializable;
import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * This encapsulates the execution of search against different indexing
 * mechanisms.
 * 
 * Canned queries have been translated into the query string by this stage.
 * Handling of parameterisation is left to the implementation.
 * 
 * @author Andy hind
 * 
 */
@AlfrescoPublicApi
public interface SearchService
{
    public static final String LANGUAGE_LUCENE = "lucene";

    public static final String LANGUAGE_XPATH = "xpath";

    public static final String LANGUAGE_FTS_ALFRESCO = "fts-alfresco";
    
    public static final String LANGUAGE_SOLR_FTS_ALFRESCO = "solr-fts-alfresco";
    
    public static final String LANGUAGE_CMIS_STRICT = "cmis-strict";
    
    public static final String LANGUAGE_CMIS_ALFRESCO = "cmis-alfresco";

    public static final String LANGUAGE_SOLR_CMIS = "solr-cmis";

    public static final String LANGUAGE_SOLR_ALFRESCO = "solr-alfresco";

    /**
     * Search against a store. Pulls back all attributes on each node. Does not
     * allow parameterisation.
     * 
     * @param store -
     *            the store against which to search
     * @param language -
     *            the query language
     * @param query -
     *            the query string - which may include parameters
     * @return Returns the query results
     */
    @Auditable(parameters = {"store", "language", "query"})
    public ResultSet query(StoreRef store, String language, String query);

    /**
     * Search against a store.
     * 
     * @param store -
     *            the store against which to search
     * @param language -
     *            the query language
     * @param query -
     *            the query string - which may include parameters
     * @param queryParameterDefinitions -
     *            query parameter definitions - the default value is used for
     *            the value.
     * @return Returns the query results
     */
    @Auditable(parameters = {"store", "language", "query", "queryParameterDefinitions"})
    public ResultSet query(StoreRef store, String language, String query,
            QueryParameterDefinition[] queryParameterDefinitions);

    
    /**
     * Execute a canned query
     * 
     * @param store -
     *            the store against which to search
     * @param queryId -
     *            the query identifier
     * @param queryParameters -
     *            parameterisation for the canned query
     * @return Returns the query results
     */
    @Auditable(parameters = {"store", "queryId", "queryParameters"})
    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters);

    /**
     * Search using the given SearchParameters
     */

    @Auditable(parameters = {"searchParameters"})
    public ResultSet query(SearchParameters searchParameters);

    /**
     * Select nodes using an xpath expression.
     * 
     * @param contextNodeRef -
     *            the context node for relative expressions etc
     * @param xpath -
     *            the xpath string to evaluate
     * @param parameters -
     *            parameters to bind in to the xpath expression, may be null for no parameters
     * @param namespacePrefixResolver -
     *            prefix to namespace mappings
     * @param followAllParentLinks -
     *            if false ".." follows only the primary parent links, if true
     *            it follows all
     * @return a list of the node refs of the selected nodes
     */
    @Auditable(
            
            parameters = {"contextNodeRef", "xpath", "parameters", "namespacePrefixResolver", "followAllParentLinks"},
            recordable = {true,             true,    true,         false,                     true})
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks)
            throws InvalidNodeRefException, XPathException;

    /**
     * Select nodes using an xpath expression.
     * 
     * @param contextNodeRef -
     *            the context node for relative expressions etc
     * @param xpath -
     *            the xpath string to evaluate
     * @param parameters -
     *            parameters to bind in to the xpath expression, may be null for no parameters
     * @param namespacePrefixResolver -
     *            prefix to namespace mappings
     * @param followAllParentLinks -
     *            if false ".." follows only the primary parent links, if true
     *            it follows all
     * @param language -
     *            the xpath variant
     * @return a list of all the node refs of the selected nodes
     */
    @Auditable(
            
            parameters = {"contextNodeRef", "xpath", "parameters", "namespacePrefixResolver", "followAllParentLinks", "language"},
            recordable = {true,             true,    true,         false,                     true,                   true})
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language)
            throws InvalidNodeRefException, XPathException;

    /**
     * Select properties using an xpath expression
     * 
     * @param contextNodeRef -
     *            the context node for relative expressions etc
     * @param xpath -
     *            the xpath string to evaluate
     * @param parameters -
     *            parameters to bind in to the xpath expression
     * @param namespacePrefixResolver -
     *            prefix to namespace mappings
     * @param followAllParentLinks -
     *            if false ".." follows only the primary parent links, if true
     *            it follows all
     * @return a list of property values
     */
    @Auditable(
            
            parameters = {"contextNodeRef", "xpath", "parameters", "namespacePrefixResolver", "followAllParentLinks"},
            recordable = {true,             true,    true,         false,                     true})
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath,
            QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException;

    /**
     * Select properties using an xpath expression
     * 
     * @param contextNodeRef -
     *            the context node for relative expressions etc
     * @param xpath -
     *            the xpath string to evaluate
     * @param parameters -
     *            parameters to bind in to the xpath expression
     * @param namespacePrefixResolver -
     *            prefix to namespace mappings
     * @param followAllParentLinks -
     *            if false ".." follows only the primary parent links, if true
     *            it follows all
     * @param language -
     *            the xpath variant
     * @return a list of property values
     */
    @Auditable(
            
            parameters = {"contextNodeRef", "xpath", "parameters", "namespacePrefixResolver", "followAllParentLinks", "language"},
            recordable = {true,             true,     true,        false,                     true,                   true})
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath,
            QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException;

    /**
     * Search for string pattern in both the node text (if present) and node
     * properties
     * 
     * @param nodeRef
     *            the node to get
     * @param propertyQName
     *            the name of the property
     * @param googleLikePattern
     *            a Google-like pattern to search for in the property value
     * @return Returns true if the pattern could be found - uses the default OR operator
     */
    @Auditable(parameters = {"nodeRef", "propertyQName", "googleLikePattern"})
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern)
            throws InvalidNodeRefException;
    
    /**
     * Search for string pattern in both the node text (if present) and node
     * properties
     * 
     * @param nodeRef
     *            the node to get
     * @param propertyQName
     *            the name of the property
     * @param googleLikePattern
     *            a Google-like pattern to search for in the property value
     * @return Returns true if the pattern could be found
     */
    @Auditable(parameters = {"nodeRef", "propertyQName", "googleLikePattern", "defaultOperator"})
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern, SearchParameters.Operator defaultOperator)
            throws InvalidNodeRefException;

    /**
     * Search for string pattern in both the node text (if present) and node
     * properties
     * 
     * @param nodeRef
     *            the node to get
     * @param propertyQName
     *            the name of the property (mandatory)
     * @param sqlLikePattern
     *            a SQL-like pattern to search for
     * @param includeFTS -
     *            include full text search matches in the like test
     * @return Returns true if the pattern could be found
     */
    @Auditable(parameters = {"nodeRef", "propertyQName", "sqlLikePattern", "includeFTS"})
    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS)
            throws InvalidNodeRefException;
}
