/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import static org.alfresco.repo.search.impl.QueryParserUtils.matchPropertyDefinition;
import static org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.QueryConverter.fromLuceneSpanToElasticsearchSpan;
import static org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.QueryConverter.fromLuceneToElasticsearch;

import java.util.List;
import java.util.Optional;

import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchNoneQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.repo.search.adaptor.AnalysisMode;
import org.alfresco.repo.search.adaptor.LuceneFunction;
import org.alfresco.repo.search.adaptor.QueryConstants;
import org.alfresco.repo.search.adaptor.QueryParserAdaptor;
import org.alfresco.repo.search.adaptor.QueryParserExpressionAdaptor;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;
import org.alfresco.repo.search.impl.elasticsearch.query.StoreRefStripper;
import org.alfresco.repo.search.impl.elasticsearch.query.language.EsTypeResolver;
import org.alfresco.repo.search.impl.elasticsearch.query.language.FieldQueryTransformer;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

/**
 * Query Parser Adaptor for Elasticsearch Elasticsearch QueryBuilder object is updated by this class in order to include the search conditions expressed in the SearchParameters object.
 */
public class AFTSQueryParserAdaptor implements QueryParserAdaptor<org.opensearch.client.opensearch._types.query_dsl.Query, Sort, ParseException>
{
    public static final String RANGE_MIN = "MIN";
    public static final String RANGE_MIN_CHAR = "\u0000";
    public static final String RANGE_MAX = "MAX";
    public static final String RANGE_MAX_CHAR = "\uFFFF";
    public static final String TODAY = "TODAY";
    public static final String TODAY_START_OF_THE_DAY = "now/d";
    public static final String TOMORROW_START_OF_THE_DAY = "now+1d/d";

    protected final SearchParameters searchParameters;
    protected final LuceneQueryParser lucene;
    protected final NamespacePrefixResolver namespaceResolver;
    protected final DictionaryService dictionaryService;
    protected final EsTypeResolver esTypeResolver;

    protected final FieldQueryTransformer fieldQueryTransformer = FieldQueryTransformer.DEFAULT;

    protected final StoreRefStripper storeRefStripper;

    public AFTSQueryParserAdaptor(SearchParameters searchParameters,
            NamespacePrefixResolver namespacePrefixResolver,
            DictionaryService dictionaryService,
            SiteService siteService,
            ElasticsearchExactTermSearchConfig exactTermSearchConfig,
            EsTypeResolver esTypeResolver)
    {
        this.searchParameters = searchParameters;
        this.namespaceResolver = namespacePrefixResolver;
        this.dictionaryService = dictionaryService;
        this.esTypeResolver = esTypeResolver;
        this.lucene = new LuceneQueryParser(namespacePrefixResolver, dictionaryService, siteService, searchParameters, exactTermSearchConfig);
        this.storeRefStripper = new StoreRefStripper(namespacePrefixResolver, dictionaryService,
                searchParameters.getNamespace());
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getFieldQuery(String fieldName, String term)
    {
        if (TODAY.equalsIgnoreCase(term) && isDate(fieldName))
        {
            return getRangeQuery(fieldName, TODAY_START_OF_THE_DAY, TOMORROW_START_OF_THE_DAY, true, false, AnalysisMode.DEFAULT, LuceneFunction.FIELD);
        }
        term = fieldQueryTransformer.transformTerm(fieldName, term);
        if (fieldName == null)
        {
            fieldName = QueryConstants.FIELD_TEXT;
        }
        Query luceneQuery = lucene.getFieldQuery(fieldName, term, false);

        if (luceneQuery instanceof SpanQuery)
        {
            return fromLuceneSpanToElasticsearchSpan((SpanQuery) luceneQuery);
        }

        return safeQueryStringQueryBuilder(luceneQuery);
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getFieldQuery(String fieldName, String term, AnalysisMode analysisMode, LuceneFunction luceneFunction)
    {
        /* This implements the exact term search */
        if (analysisMode == AnalysisMode.IDENTIFIER && isText(fieldName))
        {
            if (fieldName == null)
            {
                fieldName = QueryConstants.FIELD_TEXT;
            }
            String exactFieldName = FieldName.exactTermSearch(fieldName);
            Query luceneQuery = lucene.getFieldQuery(exactFieldName, term, false);

            return safeQueryStringQueryBuilder(luceneQuery);
        }
        else
        {
            return getFieldQuery(fieldName, term);
        }
    }

    protected boolean isText(String fieldName)
    {
        return mapToEsFieldNameAndGetEsType(fieldName)
                .map("text"::equals)
                .orElse(true);
    }

    protected boolean isKeyword(String fieldName)
    {
        return mapToEsFieldNameAndGetEsType(fieldName)
                .map("keyword"::equals)
                .orElse(true);
    }

    protected boolean isDate(String fieldName)
    {
        return mapToEsFieldNameAndGetEsType(fieldName)
                .map("date"::equals)
                .orElse(false);
    }

    private Optional<String> mapToEsFieldNameAndGetEsType(String fieldName)
    {
        return Optional.ofNullable(fieldName)
                .flatMap(lucene::stripPrefixAndSuffix)
                .map(name -> matchPropertyDefinition(searchParameters.getNamespace(), namespaceResolver,
                        dictionaryService, name))
                .map(PropertyDefinition::getName)
                .map(qName -> qName.toPrefixString(namespaceResolver))
                .map(AlfrescoQualifiedNameTranslator::encode)
                .flatMap(esTypeResolver::resolve);
    }

    /**
     * This method will be invoked when we perform a phrase query
     */
    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getFieldQuery(String fieldName, String phrase, AnalysisMode analysisMode, Integer slop, LuceneFunction luceneFunction)
    {
        phrase = storeRefStripper.stripIfNeeded(fieldName, phrase);
        phrase = fieldQueryTransformer.transformTerm(fieldName, phrase);
        if (fieldName == null)
        {
            fieldName = QueryConstants.FIELD_TEXT;
        }

        /* This implements the exact term search */
        if (analysisMode == AnalysisMode.IDENTIFIER && !lucene.isUntokenizedField(fieldName))
        {
            fieldName = FieldName.exactTermSearch(fieldName);
        }

        Query luceneQuery = lucene.getFieldQuery(fieldName, phrase, true);

        return fromLuceneToElasticsearch(luceneQuery);
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getRangeQuery(String fieldName, String lower, String upper, boolean includeLower, boolean includeUpper, AnalysisMode analysisMode, LuceneFunction luceneFunction)
    {
        if (fieldName == null)
        {
            fieldName = QueryConstants.FIELD_TEXT;
        }
        if (RANGE_MIN.equals(lower) || RANGE_MIN_CHAR.equals(lower))
        {
            lower = "*";
        }
        else
        {
            lower = DateMathConverter.convert(lower);
        }
        if (RANGE_MAX.equals(upper) || RANGE_MAX_CHAR.equals(upper))
        {
            upper = "*";
        }
        else
        {
            upper = DateMathConverter.convert(upper);
        }
        Query luceneQuery = lucene.getRangeQuery(fieldName, lower, upper, includeLower, includeUpper);

        org.opensearch.client.opensearch._types.query_dsl.Query queryStringQueryBuilder = safeQueryStringQueryBuilder(luceneQuery);
        if (queryStringQueryBuilder != null)
        {
            return queryStringQueryBuilder.queryString().toBuilder().timeZone(searchParameters.getTimezone()).build().toQuery();
        }
        else
        {
            return queryStringQueryBuilder;
        }
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getMatchAllQuery()
    {
        return QueryBuilders.matchAll().build().toQuery();
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getMatchNoneQuery() throws ParseException
    {
        return new MatchNoneQuery.Builder().build().toQuery();
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getLikeQuery(String fieldName, String term, AnalysisMode analysisMode)
    {
        return getFieldQuery(fieldName, term);
    }

    @Override
    public SearchParameters getSearchParameters()
    {
        return searchParameters;
    }

    @Override
    public String getSortField(String s)
    {
        return null;
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getIdentifierQuery(String fieldName, String term, AnalysisMode analysisMode, LuceneFunction luceneFunction)
    {
        String[] split = term.split(String.valueOf(CMISConnector.ID_SEPERATOR));
        if (split.length == 1)
        {
            return getFieldQuery(fieldName, term);
        }
        else
        {
            if (split[1].equalsIgnoreCase(CMISConnector.PWC_VERSION_LABEL))
            {
                // Match none.
                return QueryBuilders.bool().mustNot(QueryBuilders.matchAll().build().toQuery()).build().toQuery();
            }

            // Match field AND either the version OR anything that doesn't have the versionable aspect (for version 1.0).
            BoolQuery.Builder query = QueryBuilders.bool().must(getFieldQuery(fieldName, split[0]));
            BoolQuery.Builder subclause = QueryBuilders.bool().should(new org.opensearch.client.opensearch._types.query_dsl.Query.Builder().match(new MatchQuery.Builder().field("@" + ContentModel.PROP_VERSION_LABEL).query(FieldValue.of(split[1])).build()).build());
            if (split[1].equals(CMISConnector.UNVERSIONED_VERSION_LABEL))
            {
                subclause.should(QueryBuilders.bool().mustNot(getFieldQuery(QueryConstants.FIELD_ASPECT, ContentModel.ASPECT_VERSIONABLE.toString())).build().toQuery());
            }
            query.must(subclause.build().toQuery());

            return query.build().toQuery();
        }
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getIdentifieLikeQuery(String fieldName, String term, AnalysisMode analysisMode)
    {
        return null;
    }

    @Override
    public boolean sortFieldExists(String s)
    {
        return false;
    }

    @Override
    public Sort buildSort(List<Ordering> list, FunctionEvaluationContext functionEvaluationContext)
    {
        return null;
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getFuzzyQuery(String fieldName, String term, Float minSimilarity) throws ParseException
    {
        if (fieldName == null)
        {
            fieldName = QueryConstants.FIELD_TEXT;
        }
        Query luceneQuery = lucene.getFuzzyQuery(fieldName, term, minSimilarity);
        return safeQueryStringQueryBuilder(luceneQuery);
    }

    @Override
    public String getField()
    {
        return null;
    }

    @Override
    public int getPhraseSlop()
    {
        return 0;
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getPrefixQuery(String fieldName, String term, AnalysisMode analysisMode)
    {
        term = fieldQueryTransformer.transformTerm(fieldName, term);
        if (fieldName == null)
        {
            fieldName = QueryConstants.FIELD_TEXT;
        }
        /* This implements the exact term search */
        if (analysisMode == AnalysisMode.IDENTIFIER)
        {
            fieldName = FieldName.exactTermSearch(fieldName);
        }
        Query luceneQuery = lucene.getPrefixQuery(fieldName, term);
        return safeQueryStringQueryBuilder(luceneQuery);
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getSpanQuery(String fieldName, String first, String last, int slop, boolean inOrder)
    {
        first = fieldQueryTransformer.transformTerm(fieldName, first);
        last = fieldQueryTransformer.transformTerm(fieldName, last);
        if (isEmpty(first) || isEmpty(last) || slop < 0)
        {
            return null;
        }
        if (isEmpty(fieldName))
        {
            fieldName = QueryConstants.FIELD_TEXT;
        }

        Query query = lucene.getFieldQuery(fieldName, first + " " + last, slop);
        return safeQueryStringQueryBuilder(query);
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getWildcardQuery(String fieldName, String term, AnalysisMode analysisMode)
    {
        term = fieldQueryTransformer.transformTerm(fieldName, term);
        if (fieldName == null)
        {
            fieldName = QueryConstants.FIELD_TEXT;
        }
        /* This implements the exact term search */
        if (analysisMode == AnalysisMode.IDENTIFIER)
        {
            fieldName = FieldName.exactTermSearch(fieldName);
        }
        Query luceneQuery = lucene.getWildcardQuery(fieldName, term);
        return safeQueryStringQueryBuilder(luceneQuery);
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getNegatedQuery(org.opensearch.client.opensearch._types.query_dsl.Query queryBuilder)
    {
        return QueryBuilders.bool().mustNot(queryBuilder).build().toQuery();
    }

    @Override
    public QueryParserExpressionAdaptor<org.opensearch.client.opensearch._types.query_dsl.Query, ParseException> getExpressionAdaptor()
    {
        return new AFTSBooleanOperatorsAdaptor();
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getMatchAllNodesQuery()
    {
        return QueryBuilders.matchAll().build().toQuery();
    }

    @Override
    public String getDatetimeSortField(String s, PropertyDefinition propertyDefinition)
    {
        return null;
    }

    protected org.opensearch.client.opensearch._types.query_dsl.Query safeQueryStringQueryBuilder(Query luceneQuery)
    {
        return luceneQuery != null ? QueryBuilders.queryString().query(luceneQuery.toString()).build().toQuery() : null;
    }
}
