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
package org.alfresco.repo.search.impl.solr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.CannedQueryDef;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.QueryParameterisationException;
import org.alfresco.repo.tenant.TenantService;
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
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.SearchLanguageConversion;

/**
 * @author Andy
 */
public class SolrSearchService implements SearchService
{

    private NodeService nodeService;
    
    private TenantService tenantService;
    
    private NamespacePrefixResolver namespacePrefixResolver;
    
    private DictionaryService dictionaryService;
    
    private Map<String, LuceneQueryLanguageSPI> queryLanguages;
    
    private QueryRegisterComponent queryRegister;
        
    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public TenantService getTenantService()
    {
        return tenantService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return namespacePrefixResolver;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public Map<String, LuceneQueryLanguageSPI> getQueryLanguages()
    {
        return queryLanguages;
    }

    public void setQueryLanguages(Map<String, LuceneQueryLanguageSPI> queryLanguages)
    {
        this.queryLanguages = queryLanguages;
    }

    public QueryRegisterComponent getQueryRegister()
    {
        return queryRegister;
    }

    public void setQueryRegister(QueryRegisterComponent queryRegister)
    {
        this.queryRegister = queryRegister;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef,
     * java.lang.String, java.lang.String)
     */
    @Override
    public ResultSet query(StoreRef store, String language, String query)
    {
        return query(store, language, query, null);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef,
     * java.lang.String, java.lang.String, org.alfresco.service.cmr.search.QueryParameterDefinition[])
     */
    @Override
    public ResultSet query(StoreRef store, String language, String query, QueryParameterDefinition[] queryParameterDefinitions)
    {
        store = tenantService.getName(store);

        SearchParameters sp = new SearchParameters();
        sp.addStore(store);
        sp.setLanguage(language);
        sp.setQuery(query);
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.repository.StoreRef,
     * org.alfresco.service.namespace.QName, org.alfresco.service.cmr.search.QueryParameter[])
     */
    @Override
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#query(org.alfresco.service.cmr.search.SearchParameters)
     */
    @Override
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
            return language.executeQuery(searchParameters, null);
        }
        else
        {
            throw new SearcherException("Unknown query language: " + searchParameters.getLanguage());
        }
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.SearchService#like(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.namespace.QName, java.lang.String, boolean)
     */
    @Override
    public boolean like(NodeRef nodeRef, QName propertyQName, String sqlLikePattern, boolean includeFTS) throws InvalidNodeRefException
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

}
