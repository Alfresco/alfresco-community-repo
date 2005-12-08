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
package org.alfresco.service.cmr.search;

import java.io.Serializable;
import java.util.List;

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
    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters);

    /**
     * Search using the given SearchParameters
     */

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
    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS)
            throws InvalidNodeRefException;
}
