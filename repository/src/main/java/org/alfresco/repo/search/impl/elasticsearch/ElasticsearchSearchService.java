/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TAG;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.search.AbstractSearcherComponent;
import org.alfresco.repo.search.CannedQueryDef;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.repo.search.impl.QueryParameterisationException;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
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
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Query executor for Elasticsearch servers.
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.CallSuperInConstructor", "PMD.UseLocaleWithCaseConversions"})
public class ElasticsearchSearchService extends AbstractSearcherComponent
{

    // Used regex may not find all possible lucene path query variations, but should cover most common cases.
    private static final Pattern PATH_TO_TAGGABLE = Pattern.compile("\\+?PATH:\"/cm:taggable/cm:([^/]+)/member\"");
    private final QueryRegisterComponent queryRegister;
    private final Map<String, LuceneQueryLanguageSPI> queryLanguages;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private NamespacePrefixResolver namespacePrefixResolver;

    public ElasticsearchSearchService(QueryRegisterComponent queryRegister,
            Map<String, LuceneQueryLanguageSPI> queryLanguages,
            NodeService nodeService,
            DictionaryService dictionaryService,
            NamespacePrefixResolver namespacePrefixResolver)
    {
        this.queryRegister = queryRegister;
        this.queryLanguages = queryLanguages;
        this.nodeService = nodeService;
        this.dictionaryService = dictionaryService;
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    @Override
    public ResultSet query(StoreRef store, String language, String query)
    {
        return query(store, language, query, null);
    }

    @Override
    public ResultSet query(StoreRef store, String language, String query, QueryParameterDefinition[] queryParameterDefinitions)
    {
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

    @Override
    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        CannedQueryDef definition = queryRegister.getQueryDefinition(queryId);

        return query(store, definition.getLanguage(), queryParameters.toString(), null);
    }

    @Override
    public ResultSet query(SearchParameters searchParameters)
    {
        adjustSearchParameters(searchParameters);
        parameteriseQuery(searchParameters);
        LuceneQueryLanguageSPI language = queryLanguages.get(searchParameters.getLanguage().toLowerCase());
        if (language != null)
        {
            return language.executeQuery(searchParameters);
        }
        else
        {
            throw new SearcherException("Unknown query language: " + searchParameters.getLanguage());
        }
    }

    private void parameteriseQuery(final SearchParameters searchParameters)
    {
        if (searchParameters.getQuery() == null || searchParameters.getQueryParameterDefinitions().isEmpty())
        {
            return;
        }

        Map<QName, QueryParameterDefinition> map = new HashMap<>();
        for (QueryParameterDefinition qpd : searchParameters.getQueryParameterDefinitions())
        {
            map.put(qpd.getQName(), qpd);
        }

        String parameterisedQueryString = parameterise(searchParameters.getQuery(), map, null, namespacePrefixResolver);
        searchParameters.setQuery(parameterisedQueryString);
    }

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

    @Override
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks)
            throws InvalidNodeRefException, XPathException
    {
        return selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, LANGUAGE_XPATH);
    }

    @Override
    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks, String language)
            throws InvalidNodeRefException, XPathException
    {
        NodeSearcher nodeSearcher = new NodeSearcher(nodeService, dictionaryService, this);
        return nodeSearcher.selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks, language);
    }

    @Override
    public List<Serializable> selectProperties(NodeRef nodeRef, String s,
            QueryParameterDefinition[] queryParameterDefinitions, NamespacePrefixResolver namespacePrefixResolver,
            boolean b, String s1) throws InvalidNodeRefException, XPathException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(NodeRef nodeRef, QName qName, String s) throws InvalidNodeRefException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(NodeRef nodeRef, QName qName, String s, Operator operator) throws InvalidNodeRefException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean like(NodeRef nodeRef, QName qName, String s, boolean b) throws InvalidNodeRefException
    {
        throw new UnsupportedOperationException();
    }

    private void adjustSearchParameters(final SearchParameters sp)
    {
        if (isTaggablePathQuery(sp))
        {
            replaceTaggablePathQueryWithTagQuery(sp);
        }
    }

    private boolean isTaggablePathQuery(final SearchParameters sp)
    {
        if (!LANGUAGE_LUCENE.equals(sp.getLanguage()) || sp.getQuery() == null)
        {
            return false;
        }

        Matcher matcher = PATH_TO_TAGGABLE.matcher(sp.getQuery());
        return matcher.find();
    }

    private void replaceTaggablePathQueryWithTagQuery(final SearchParameters sp)
    {
        // Secondary paths are not supported yet. To support TAGs we need to translate the lucene secondary path query
        // to the afts TAG query
        String query = sp.getQuery();
        String tagQuery = query.replaceAll(PATH_TO_TAGGABLE.pattern(), FIELD_TAG + ":$1");
        sp.setQuery(tagQuery);
        sp.setLanguage(LANGUAGE_FTS_ALFRESCO);
    }
}
