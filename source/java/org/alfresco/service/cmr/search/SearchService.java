/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.search;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
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
@PublicService
public interface SearchService
{
    public static final String LANGUAGE_LUCENE = "lucene";

    public static final String LANGUAGE_XPATH = "xpath";

    public static final String LANGUAGE_JCR_XPATH = "jcr-xpath";

    /**
     * Search against a store.
     * 
     * @param store -
     *            the store against which to search
     * @param language -
     *            the query language
     * @param query -
     *            the query string - which may include parameters
     * @param attributePaths -
     *            explicit list of attributes/properties to extract for the
     *            selected nodes in xpath style syntax
     * @param queryParameterDefinition -
     *            query parameter definitions - the default value is used for
     *            the value.
     * @return Returns the query results
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"store", "language", "query", "attributePaths", "queryParameterDefinitions"})
    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths,
            QueryParameterDefinition[] queryParameterDefinitions);

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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"store", "language", "query"})
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
     * @param queryParameterDefinition -
     *            query parameter definitions - the default value is used for
     *            the value.
     * @return Returns the query results
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"store", "language", "query", "queryParameterDefinitions"})
    public ResultSet query(StoreRef store, String language, String query,
            QueryParameterDefinition[] queryParameterDefintions);

    /**
     * Search against a store.
     * 
     * @param store -
     *            the store against which to search
     * @param language -
     *            the query language
     * @param query -
     *            the query string - which may include parameters
     * @param attributePaths -
     *            explicit list of attributes/properties to extract for the
     *            selected nodes in xpath style syntax
     * @return Returns the query results
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"store", "language", "query", "attributePaths"})
    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths);

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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"store", "queryId", "queryParameters"})
    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters);

    /**
     * Search using the given SearchParameters
     */

    @Auditable(key = Auditable.Key.ARG_0, parameters = {"searchParameters"})
    public ResultSet query(SearchParameters searchParameters);

    /**
     * Select nodes using an xpath expression.
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
     * @return a list of all the child assoc relationships to the selected nodes
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef", "xpath", "parameters", "namespacePrefixResolver", "followAllParentLinks"})
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
     *            parameters to bind in to the xpath expression
     * @param namespacePrefixResolver -
     *            prefix to namespace mappings
     * @param followAllParentLinks -
     *            if false ".." follows only the primary parent links, if true
     *            it follows all
     * @param langauage -
     *            the xpath variant
     * @return a list of all the child assoc relationships to the selected nodes
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef", "xpath", "parameters", "namespacePrefixResolver", "followAllParentLinks", "language"})
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef", "xpath", "parameters", "namespacePrefixResolver", "followAllParentLinks"})
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
     * @param langauage -
     *            the xpath variant
     * @return a list of property values
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef", "xpath", "parameters", "namespacePrefixResolver", "followAllParentLinks", "language"})
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "propertyQName", "googleLikePattern"})
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "propertyQName", "googleLikePattern", "defaultOperator"})
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "propertyQName", "sqlLikePattern", "includeFTS"})
    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS)
            throws InvalidNodeRefException;
}
