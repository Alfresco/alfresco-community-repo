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
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.CannedQueryDef;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.saxpath.SAXPathException;

import com.werken.saxpath.XPathReader;

/**
 * The Lucene implementation of Searcher At the moment we support only lucene
 * based queries.
 * 
 * TODO: Support for other query languages
 * 
 * @author andyh
 * 
 */
public class LuceneSearcherImpl extends LuceneBase implements LuceneSearcher
{
    /**
     * Default field name
     */
    private static final String DEFAULT_FIELD = "TEXT";

    private NamespacePrefixResolver namespacePrefixResolver;

    private NodeService nodeService;

    private DictionaryService dictionaryService;

    private QueryRegisterComponent queryRegister;

    private LuceneIndexer indexer;

    /*
     * Searcher implementation
     */

    /**
     * Get an initialised searcher for the store and transaction Normally we do
     * not search against a a store and delta. Currently only gets the searcher
     * against the main index.
     * 
     * @param storeRef
     * @param deltaId
     * @return
     */
    public static LuceneSearcherImpl getSearcher(StoreRef storeRef, LuceneIndexer indexer, LuceneConfig config)
    {
        LuceneSearcherImpl searcher = new LuceneSearcherImpl();
        searcher.setLuceneConfig(config);
        try
        {
            searcher.initialise(storeRef, indexer == null ? null : indexer.getDeltaId(), false, false);
            searcher.indexer = indexer;
        }
        catch (LuceneIndexException e)
        {
            throw new SearcherException(e);
        }
        return searcher;
    }

    /**
     * Get an intialised searcher for the store. No transactional ammendsmends
     * are searched.
     * 
     * 
     * @param storeRef
     * @return
     */
    public static LuceneSearcherImpl getSearcher(StoreRef storeRef, LuceneConfig config)
    {
        return getSearcher(storeRef, null, config);
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public boolean indexExists()
    {
        return mainIndexExists();
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setQueryRegister(QueryRegisterComponent queryRegister)
    {
        this.queryRegister = queryRegister;
    }

    public ResultSet query(StoreRef store, String language, String queryString, Path[] queryOptions,
            QueryParameterDefinition[] queryParameterDefinitions) throws SearcherException
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(store);
        sp.setLanguage(language);
        sp.setQuery(queryString);
        if (queryOptions != null)
        {
            for (Path path : queryOptions)
            {
                sp.addAttrbutePath(path);
            }
        }
        if (queryParameterDefinitions != null)
        {
            for (QueryParameterDefinition qpd : queryParameterDefinitions)
            {
                sp.addQueryParameterDefinition(qpd);
            }
        }
        sp.excludeDataInTheCurrentTransaction(true);

        return query(sp);
    }

    public ResultSet query(SearchParameters searchParameters)
    {
        if (searchParameters.getStores().size() != 1)
        {
            throw new IllegalStateException("Only one store can be searched at present");
        }

        String parameterisedQueryString;
        if (searchParameters.getQueryParameterDefinitions().size() > 0)
        {
            Map<QName, QueryParameterDefinition> map = new HashMap<QName, QueryParameterDefinition>();

            for (QueryParameterDefinition qpd : searchParameters.getQueryParameterDefinitions())
            {
                map.put(qpd.getQName(), qpd);
            }

            parameterisedQueryString = parameterise(searchParameters.getQuery(), map, null, namespacePrefixResolver);
        }
        else
        {
            parameterisedQueryString = searchParameters.getQuery();
        }

        if (searchParameters.getLanguage().equalsIgnoreCase(SearchService.LANGUAGE_LUCENE))
        {
            try
            {

                int defaultOperator;
                if (searchParameters.getDefaultOperator() == SearchParameters.AND)
                {
                    defaultOperator = LuceneQueryParser.DEFAULT_OPERATOR_AND;
                }
                else
                {
                    defaultOperator = LuceneQueryParser.DEFAULT_OPERATOR_OR;
                }

                Query query = LuceneQueryParser.parse(parameterisedQueryString, DEFAULT_FIELD, new LuceneAnalyser(
                        dictionaryService), namespacePrefixResolver, dictionaryService, defaultOperator);
                Searcher searcher = getSearcher(indexer);
                if (searcher == null)
                {
                    // no index return an empty result set
                    return new EmptyResultSet();
                }

                Hits hits;

                if (searchParameters.getSortDefinitions().size() > 0)
                {
                    int index = 0;
                    SortField[] fields = new SortField[searchParameters.getSortDefinitions().size()];
                    for (SearchParameters.SortDefinition sd : searchParameters.getSortDefinitions())
                    {
                        switch (sd.getSortType())
                        {
                        case FIELD:
                            fields[index++] = new SortField(sd.getField(), !sd.isAscending());
                            break;
                        case DOCUMENT:
                            fields[index++] = new SortField(null, SortField.DOC, !sd.isAscending());
                            break;
                        case SCORE:
                            fields[index++] = new SortField(null, SortField.SCORE, !sd.isAscending());
                            break;
                        }

                    }
                    hits = searcher.search(query, new Sort(fields));
                }
                else
                {
                    hits = searcher.search(query);
                }

                return new LuceneResultSet(hits, searcher, nodeService, searchParameters.getAttributePaths().toArray(
                        new Path[0]), searchParameters);

            }
            catch (ParseException e)
            {
                throw new SearcherException("Failed to parse query: " + parameterisedQueryString, e);
            }
            catch (IOException e)
            {
                throw new SearcherException("IO exception during search", e);
            }
        }
        else if (searchParameters.getLanguage().equalsIgnoreCase(SearchService.LANGUAGE_XPATH))
        {
            try
            {
                XPathReader reader = new XPathReader();
                LuceneXPathHandler handler = new LuceneXPathHandler();
                handler.setNamespacePrefixResolver(namespacePrefixResolver);
                handler.setDictionaryService(dictionaryService);
                // TODO: Handler should have the query parameters to use in
                // building its lucene query
                // At the moment xpath style parameters in the PATH
                // expression are not supported.
                reader.setXPathHandler(handler);
                reader.parse(parameterisedQueryString);
                Query query = handler.getQuery();
                Searcher searcher = getSearcher(null);
                if (searcher == null)
                {
                    // no index return an empty result set
                    return new EmptyResultSet();
                }
                Hits hits = searcher.search(query);
                return new LuceneResultSet(hits, searcher, nodeService, searchParameters.getAttributePaths().toArray(
                        new Path[0]), searchParameters);
            }
            catch (SAXPathException e)
            {
                throw new SearcherException("Failed to parse query: " + searchParameters.getQuery(), e);
            }
            catch (IOException e)
            {
                throw new SearcherException("IO exception during search", e);
            }
        }
        else
        {
            throw new SearcherException("Unknown query language: " + searchParameters.getLanguage());
        }
    }

    public ResultSet query(StoreRef store, String language, String query)
    {
        return query(store, language, query, null, null);
    }

    public ResultSet query(StoreRef store, String language, String query,
            QueryParameterDefinition[] queryParameterDefintions)
    {
        return query(store, language, query, null, queryParameterDefintions);
    }

    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths)
    {
        return query(store, language, query, attributePaths, null);
    }

    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        CannedQueryDef definition = queryRegister.getQueryDefinition(queryId);

        // Do parameter replacement
        // As lucene phrases are tokensied it is correct to just do straight
        // string replacement.
        // The string will be formatted by the tokeniser.
        //
        // For non phrase queries this is incorrect but string replacement is
        // probably the best we can do.
        // As numbers and text are indexed specially, direct term queries only
        // make sense against textual data

        checkParameters(definition, queryParameters);

        String queryString = parameterise(definition.getQuery(), definition.getQueryParameterMap(), queryParameters,
                definition.getNamespacePrefixResolver());

        return query(store, definition.getLanguage(), queryString, null, null);
    }

    /**
     * The definitions must provide a default value, or of not there must be a
     * parameter to provide the value
     * 
     * @param definition
     * @param queryParameters
     * @throws QueryParameterisationException
     */
    private void checkParameters(CannedQueryDef definition, QueryParameter[] queryParameters)
            throws QueryParameterisationException
    {
        List<QName> missing = new ArrayList<QName>();

        Set<QName> parameterQNameSet = new HashSet<QName>();
        if (queryParameters != null)
        {
            for (QueryParameter parameter : queryParameters)
            {
                parameterQNameSet.add(parameter.getQName());
            }
        }

        for (QueryParameterDefinition parameterDefinition : definition.getQueryParameterDefs())
        {
            if (!parameterDefinition.hasDefaultValue())
            {
                if (!parameterQNameSet.contains(parameterDefinition.getQName()))
                {
                    missing.add(parameterDefinition.getQName());
                }
            }
        }

        if (missing.size() > 0)
        {
            StringBuilder buffer = new StringBuilder(128);
            buffer.append("The query is missing values for the following parameters: ");
            for (QName qName : missing)
            {
                buffer.append(qName);
                buffer.append(", ");
            }
            buffer.delete(buffer.length() - 1, buffer.length() - 1);
            buffer.delete(buffer.length() - 1, buffer.length() - 1);
            throw new QueryParameterisationException(buffer.toString());
        }
    }

    /*
     * Parameterise the query string - not sure if it is required to escape
     * lucence spacials chars The parameters could be used to build the query -
     * the contents of parameters should alread have been escaped if required.
     * ... mush better to provide the parameters and work out what to do TODO:
     * conditional query escapement - may be we should have a parameter type
     * that is not escaped
     */
    private String parameterise(String unparameterised, Map<QName, QueryParameterDefinition> map,
            QueryParameter[] queryParameters, NamespacePrefixResolver nspr) throws QueryParameterisationException
    {

        Map<QName, List<Serializable>> valueMap = new HashMap<QName, List<Serializable>>();

        if (queryParameters != null)
        {
            for (QueryParameter parameter : queryParameters)
            {
                List<Serializable> list = valueMap.get(parameter.getQName());
                if (list == null)
                {
                    list = new ArrayList<Serializable>();
                    valueMap.put(parameter.getQName(), list);
                }
                list.add(parameter.getValue());
            }
        }

        Map<QName, ListIterator<Serializable>> iteratorMap = new HashMap<QName, ListIterator<Serializable>>();

        List<QName> missing = new ArrayList<QName>(1);
        StringBuilder buffer = new StringBuilder(unparameterised);
        int index = 0;
        while ((index = buffer.indexOf("${", index)) != -1)
        {
            int endIndex = buffer.indexOf("}", index);
            String qNameString = buffer.substring(index + 2, endIndex);
            QName key = QName.createQName(qNameString, nspr);
            QueryParameterDefinition parameterDefinition = map.get(key);
            if (parameterDefinition == null)
            {
                missing.add(key);
                buffer.replace(index, endIndex + 1, "");
            }
            else
            {
                ListIterator<Serializable> it = iteratorMap.get(key);
                if ((it == null) || (!it.hasNext()))
                {
                    List<Serializable> list = valueMap.get(key);
                    if ((list != null) && (list.size() > 0))
                    {
                        it = list.listIterator();
                    }
                    if (it != null)
                    {
                        iteratorMap.put(key, it);
                    }
                }
                String value;
                if (it == null)
                {
                    value = parameterDefinition.getDefault();
                }
                else
                {
                    value = DefaultTypeConverter.INSTANCE.convert(String.class, it.next());
                }
                buffer.replace(index, endIndex + 1, value);
            }
        }
        if (missing.size() > 0)
        {
            StringBuilder error = new StringBuilder();
            error.append("The query uses the following parameters which are not defined: ");
            for (QName qName : missing)
            {
                error.append(qName);
                error.append(", ");
            }
            error.delete(error.length() - 1, error.length() - 1);
            error.delete(error.length() - 1, error.length() - 1);
            throw new QueryParameterisationException(error.toString());
        }
        return buffer.toString();
    }

    /**
     * @see org.alfresco.repo.search.impl.NodeSearcher
     */
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language)
            throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, dictionaryService, this);
        return nodeSearcher.selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver,
                followAllParentLinks, language);
    }

    /**
     * @see org.alfresco.repo.search.impl.NodeSearcher
     */
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath,
            QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, dictionaryService, this);
        return nodeSearcher.selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver,
                followAllParentLinks, language);
    }

    /**
     * @return Returns true if the pattern is present, otherwise false.
     */
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern)
    {
        return contains(nodeRef, propertyQName, googleLikePattern, SearchParameters.Operator.OR);
    }

    /**
     * @return Returns true if the pattern is present, otherwise false.
     */
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern,
            SearchParameters.Operator defaultOperator)
    {
        ResultSet resultSet = null;
        try
        {
            // build Lucene search string specific to the node
            StringBuilder sb = new StringBuilder();
            sb.append("+ID:\"").append(nodeRef.toString()).append("\" +(TEXT:(")
                    .append(googleLikePattern.toLowerCase()).append(") ");
            if (propertyQName != null)
            {
                sb.append(" OR @").append(
                        LuceneQueryParser.escape(QName.createQName(propertyQName.getNamespaceURI(),
                                ISO9075.encode(propertyQName.getLocalName())).toString()));
                sb.append(":(").append(googleLikePattern.toLowerCase()).append(")");
            }
            else
            {
                for (QName key : nodeService.getProperties(nodeRef).keySet())
                {
                    sb.append(" OR @").append(
                            LuceneQueryParser.escape(QName.createQName(key.getNamespaceURI(),
                                    ISO9075.encode(key.getLocalName())).toString()));
                    sb.append(":(").append(googleLikePattern.toLowerCase()).append(")");
                }
            }
            sb.append(")");

            SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(sb.toString());
            sp.setDefaultOperator(defaultOperator);
            sp.addStore(nodeRef.getStoreRef());

            resultSet = this.query(sp);
            boolean answer = resultSet.length() > 0;
            return answer;
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    /**
     * @return Returns true if the pattern is present, otherwise false.
     * 
     * @see #setIndexer(Indexer)
     * @see #setSearcher(SearchService)
     */
    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS)
    {
        if (propertyQName == null)
        {
            throw new IllegalArgumentException("Property QName is mandatory for the like expression");
        }

        StringBuilder sb = new StringBuilder(sqlLikePattern.length() * 3);

        if (includeFTS)
        {
            // convert the SQL-like pattern into a Lucene-compatible string
            String pattern = SearchLanguageConversion.convertXPathLikeToLucene(sqlLikePattern.toLowerCase());

            // build Lucene search string specific to the node
            sb = new StringBuilder();
            sb.append("+ID:\"").append(nodeRef.toString()).append("\" +(");
            // FTS or attribute matches
            if (includeFTS)
            {
                sb.append("TEXT:(").append(pattern).append(") ");
            }
            if (propertyQName != null)
            {
                sb.append(" @").append(
                        LuceneQueryParser.escape(QName.createQName(propertyQName.getNamespaceURI(),
                                ISO9075.encode(propertyQName.getLocalName())).toString())).append(":(").append(pattern)
                        .append(")");
            }
            sb.append(")");

            ResultSet resultSet = null;
            try
            {
                resultSet = this.query(nodeRef.getStoreRef(), "lucene", sb.toString());
                boolean answer = resultSet.length() > 0;
                return answer;
            }
            finally
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }
            }
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
                String propertyString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(
                        nodeRef, propertyQName));
                return propertyString.toLowerCase().matches(pattern);
            }
        }
    }

    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks)
            throws InvalidNodeRefException, XPathException
    {
        return selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks,
                SearchService.LANGUAGE_XPATH);
    }

    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath,
            QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks,
                SearchService.LANGUAGE_XPATH);
    }
}
