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
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISResultSetMetaData;
import org.alfresco.cmis.CMISResultSetRow;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.search.CannedQueryDef;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.repo.search.impl.parsers.FTSQueryParser;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.results.ResultSetSPIWrapper;
import org.alfresco.repo.search.results.SortedResultSet;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.saxpath.SAXPathException;

import com.werken.saxpath.XPathReader;

/**
 * The Lucene implementation of Searcher At the moment we support only lucene based queries. TODO: Support for other
 * query languages
 * 
 * @author andyh
 */
public class ADMLuceneSearcherImpl extends AbstractLuceneBase implements LuceneSearcher
{
    static Log s_logger = LogFactory.getLog(ADMLuceneSearcherImpl.class);

    private NamespacePrefixResolver namespacePrefixResolver;

    private NodeService nodeService;

    private TenantService tenantService;

    private QueryRegisterComponent queryRegister;

    private LuceneIndexer indexer;

    private Map<String, LuceneQueryLanguageSPI> queryLanguages;

    /*
     * Searcher implementation
     */

    /**
     * Get an initialised searcher for the store and transaction Normally we do not search against a a store and delta.
     * Currently only gets the searcher against the main index.
     * 
     * @param storeRef
     * @param indexer
     * @param config
     * @return - the searcher implementation
     */
    public static ADMLuceneSearcherImpl getSearcher(StoreRef storeRef, LuceneIndexer indexer, LuceneConfig config)
    {
        ADMLuceneSearcherImpl searcher = new ADMLuceneSearcherImpl();
        searcher.setLuceneConfig(config);
        try
        {
            searcher.initialise(storeRef, indexer == null ? null : indexer.getDeltaId());
            searcher.indexer = indexer;
        }
        catch (LuceneIndexException e)
        {
            throw new SearcherException(e);
        }
        return searcher;
    }

    /**
     * Get an intialised searcher for the store. No transactional ammendsmends are searched.
     * 
     * @param storeRef
     * @param config
     * @return the searcher
     */
    public static ADMLuceneSearcherImpl getSearcher(StoreRef storeRef, LuceneConfig config)
    {
        return getSearcher(storeRef, null, config);
    }

    /**
     * Get a select-node-based searcher
     * 
     * @return
     */
    public static ADMLuceneSearcherImpl getNodeSearcher()
    {
        return new ADMLuceneSearcherImpl();
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return namespacePrefixResolver;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public TenantService getTenantService()
    {
        return tenantService;
    }

    public QueryRegisterComponent getQueryRegister()
    {
        return queryRegister;
    }

    public boolean indexExists()
    {
        // return mainIndexExists();
        return true;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Set the query register
     * 
     * @param queryRegister
     */
    public void setQueryRegister(QueryRegisterComponent queryRegister)
    {
        this.queryRegister = queryRegister;
    }

    public void setQueryLanguages(Map<String, LuceneQueryLanguageSPI> queryLanguages)
    {
        this.queryLanguages = queryLanguages;
    }

    public ResultSet query(StoreRef store, String language, String queryString, QueryParameterDefinition[] queryParameterDefinitions) throws SearcherException
    {
        store = tenantService.getName(store);

        SearchParameters sp = new SearchParameters();
        sp.addStore(store);
        sp.setLanguage(language);
        sp.setQuery(queryString);
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

        ArrayList<StoreRef> stores = searchParameters.getStores();
        stores.set(0, tenantService.getName(searchParameters.getStores().get(0)));

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
        // TODO: add another property so the set query is not changed ...
        // May be good to return the query as run ??
        searchParameters.setQuery(parameterisedQueryString);

        LuceneQueryLanguageSPI language = queryLanguages.get(searchParameters.getLanguage().toLowerCase());
        if (language != null)
        {
            return language.executeQuery(searchParameters, this);
        }
        else
        {
            throw new SearcherException("Unknown query language: " + searchParameters.getLanguage());
        }        
    }


    Locale getLocale(SearchParameters searchParameters)
    {
        List<Locale> locales = searchParameters.getLocales();
        if (((locales == null) || (locales.size() == 0)))
        {
            locales = Collections.singletonList(I18NUtil.getLocale());
        }

        if (locales.size() > 1)
        {
            throw new SearcherException("Order on text/mltext properties with more than one locale is not curently supported");
        }

        Locale sortLocale = locales.get(0);
        return sortLocale;
    }
    
    String findSortField(SearchParameters searchParameters, ClosingIndexSearcher searcher, String field, Locale sortLocale)
    {
        // find best field match

        MLAnalysisMode analysisMode = getLuceneConfig().getDefaultMLSearchAnalysisMode();
        HashSet<String> allowableLocales = new HashSet<String>();
        for (Locale l : MLAnalysisMode.getLocales(analysisMode, sortLocale, false))
        {
            allowableLocales.add(l.toString());
        }

        String sortField = field;

        for (Object current : searcher.getReader().getFieldNames(FieldOption.INDEXED))
        {
            String currentString = (String) current;
            if (currentString.startsWith(field) && currentString.endsWith(".sort"))
            {
                String fieldLocale = currentString.substring(field.length() + 1, currentString.length() - 5);
                if (allowableLocales.contains(fieldLocale))
                {
                    if (fieldLocale.equals(sortLocale.toString()))
                    {
                        sortField = currentString;
                        break;
                    }
                    else if (sortLocale.toString().startsWith(fieldLocale))
                    {
                        if (sortField.equals(field) || (currentString.length() < sortField.length()))
                        {
                            sortField = currentString;
                        }
                    }
                    else if (fieldLocale.startsWith(sortLocale.toString()))
                    {
                        if (sortField.equals(field) || (currentString.length() < sortField.length()))
                        {
                            sortField = currentString;
                        }
                    }
                }
            }
        }
        return sortField;

    }

    public ResultSet query(StoreRef store, String language, String query)
    {
        return query(store, language, query, null);
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

        String queryString = parameterise(definition.getQuery(), definition.getQueryParameterMap(), queryParameters, definition.getNamespacePrefixResolver());

        return query(store, definition.getLanguage(), queryString, null);
    }

    /**
     * The definitions must provide a default value, or of not there must be a parameter to provide the value
     * 
     * @param definition
     * @param queryParameters
     * @throws QueryParameterisationException
     */
    private void checkParameters(CannedQueryDef definition, QueryParameter[] queryParameters) throws QueryParameterisationException
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
     * Parameterise the query string - not sure if it is required to escape lucence spacials chars The parameters could
     * be used to build the query - the contents of parameters should alread have been escaped if required. ... mush
     * better to provide the parameters and work out what to do TODO: conditional query escapement - may be we should
     * have a parameter type that is not escaped
     */
    private String parameterise(String unparameterised, Map<QName, QueryParameterDefinition> map, QueryParameter[] queryParameters, NamespacePrefixResolver nspr)
            throws QueryParameterisationException
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
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, getDictionaryService(), this);
        return nodeSearcher.selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }

    /**
     * @see org.alfresco.repo.search.impl.NodeSearcher
     */
    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks, String language) throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, getDictionaryService(), this);
        return nodeSearcher.selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
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
    public boolean contains(NodeRef nodeRef, QName propertyQName, String googleLikePattern, SearchParameters.Operator defaultOperator)
    {
        ResultSet resultSet = null;
        try
        {
            // build Lucene search string specific to the node
            StringBuilder sb = new StringBuilder();
            sb.append("+ID:\"").append(nodeRef.toString()).append("\" +(TEXT:(").append(googleLikePattern.toLowerCase()).append(") ");
            if (propertyQName != null)
            {
                sb.append(" OR @").append(LuceneQueryParser.escape(QName.createQName(propertyQName.getNamespaceURI(), ISO9075.encode(propertyQName.getLocalName())).toString()));
                sb.append(":(").append(googleLikePattern.toLowerCase()).append(")");
            }
            else
            {
                for (QName key : nodeService.getProperties(nodeRef).keySet())
                {
                    sb.append(" OR @").append(LuceneQueryParser.escape(QName.createQName(key.getNamespaceURI(), ISO9075.encode(key.getLocalName())).toString()));
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
                sb.append(" @").append(LuceneQueryParser.escape(QName.createQName(propertyQName.getNamespaceURI(), ISO9075.encode(propertyQName.getLocalName())).toString()))
                        .append(":(").append(pattern).append(")");
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
                String propertyString = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, propertyQName));
                return propertyString.toLowerCase().matches(pattern);
            }
        }
    }

    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, SearchService.LANGUAGE_XPATH);
    }

    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, SearchService.LANGUAGE_XPATH);
    }

    String expandAttributeFieldName(String field)
    {
        String fieldName = field;
        // Check for any prefixes and expand to the full uri
        if (field.charAt(1) != '{')
        {
            int colonPosition = field.indexOf(':');
            if (colonPosition == -1)
            {
                // use the default namespace
                fieldName = "@{" + namespacePrefixResolver.getNamespaceURI("") + "}" + field.substring(1);
            }
            else
            {
                // find the prefix
                fieldName = "@{" + namespacePrefixResolver.getNamespaceURI(field.substring(1, colonPosition)) + "}" + field.substring(colonPosition + 1);
            }
        }
        return fieldName;
    }

    public List<Pair<String, Integer>> getTopTerms(String field, int count)
    {
        ClosingIndexSearcher searcher = null;
        try
        {
            LinkedList<Pair<String, Integer>> answer = new LinkedList<Pair<String, Integer>>();
            searcher = getSearcher(indexer);
            IndexReader reader = searcher.getIndexReader();
            TermEnum terms = reader.terms(new Term(field, ""));
            do
            {
                Term term = terms.term();
                if (term != null)
                {
                    if (!term.field().equals(field))
                    {
                        break;
                    }
                    int freq = terms.docFreq();
                    Pair<String, Integer> pair = new Pair<String, Integer>(term.text(), Integer.valueOf(freq));
                    if (answer.size() < count)
                    {
                        if (answer.size() == 0)
                        {
                            answer.add(pair);
                        }
                        else if (answer.get(answer.size() - 1).getSecond().compareTo(pair.getSecond()) >= 0)
                        {
                            answer.add(pair);
                        }
                        else
                        {
                            for (ListIterator<Pair<String, Integer>> it = answer.listIterator(); it.hasNext(); /**/)
                            {
                                Pair<String, Integer> test = it.next();
                                if (test.getSecond().compareTo(pair.getSecond()) < 0)
                                {
                                    it.previous();
                                    it.add(pair);
                                    break;
                                }
                            }
                        }
                    }
                    else if (answer.get(count - 1).getSecond().compareTo(pair.getSecond()) < 0)
                    {
                        for (ListIterator<Pair<String, Integer>> it = answer.listIterator(); it.hasNext(); /**/)
                        {
                            Pair<String, Integer> test = it.next();
                            if (test.getSecond().compareTo(pair.getSecond()) < 0)
                            {
                                it.previous();
                                it.add(pair);
                                break;
                            }
                        }
                        answer.removeLast();
                    }
                    else
                    {
                        // off the end
                    }
                }
            }
            while (terms.next());
            terms.close();
            return answer;

        }
        catch (IOException e)
        {
            throw new SearcherException(e);
        }
        finally
        {
            if (searcher != null)
            {
                try
                {
                    searcher.close();
                }
                catch (IOException e)
                {
                    throw new SearcherException(e);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.lucene.LuceneSearcher#getClosingIndexSearcher()
     */
    public ClosingIndexSearcher getClosingIndexSearcher()
    {
        return getSearcher(indexer);
    }

}
