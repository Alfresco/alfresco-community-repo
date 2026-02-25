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
package org.alfresco.repo.search.impl.elasticsearch.query.aggregation;

import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import static org.alfresco.repo.search.impl.elasticsearch.util.CollectionUtils.safe;
import static org.alfresco.repo.search.adaptor.QueryConstants.PROPERTY_FIELD_PREFIX;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.lucene.queryparser.classic.ParseException;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.aggregations.AggregationBuilders;
import org.opensearch.client.opensearch._types.aggregations.Buckets;
import org.opensearch.client.opensearch._types.aggregations.FiltersAggregation;
import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;
import org.opensearch.client.opensearch._types.aggregations.TermsInclude;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilder;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;
import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchQueryHelper;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.repo.search.impl.parsers.FTSQueryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Build the filter and terms aggregation starting from the search parameters and using the specified query language.
 */
public class ElasticsearchAggregationBuilder
{

    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAggregationBuilder.class);

    private final NamespacePrefixResolver namespaceDAO;
    private final DictionaryService dictionaryService;
    private int defaultFacetLimit;

    public ElasticsearchAggregationBuilder(NamespaceDAO namespaceDAO, DictionaryService dictionaryService)
    {
        this.namespaceDAO = namespaceDAO;
        this.dictionaryService = dictionaryService;
    }

    /**
     * 
     * @param searchParameters
     * @param languageQueryBuilder
     *            the language query builder used to build the filter query
     * @return a Map of String Label and Query
     */
    public Map<String, Query> filterAggregation(SearchParameters searchParameters,
            LanguageQueryBuilder languageQueryBuilder)
    {
        Map<String, Query> result = new LinkedHashMap<>();
        List<FiltersAggregation> aggregationList = buildFilterAggregations(searchParameters, languageQueryBuilder);
        if (!CollectionUtils.isEmpty(aggregationList))
        {
            aggregationList.forEach(aggregation -> {
                if (!CollectionUtils.isEmpty(aggregation.filters().keyed()))
                {
                    result.putAll(aggregation.filters().keyed());
                }
            });
        }
        return result;
    }

    /**
     * 
     * @param parameters
     * @param languageQueryBuilder
     *            the language query builder used to build the terms aggregations query
     * @return the term aggregations stream
     */
    public Stream<TermsAggregation> termsAggregations(SearchParameters parameters,
            LanguageQueryBuilder languageQueryBuilder)
    {
        return safe(parameters.getFieldFacets()).stream().map(specs -> {
            final TermsAggregation.Builder termsBuilder = AggregationBuilders.terms().name(ofNullable(specs.getLabel()).orElse(specs.getField()))
                    .field(fieldNameFrom(specs, parameters))
                    .minDocCount(specs.getMinCount())
                    .size(defaultFacetLimit);

            ofNullable(specs.getSort()).filter(sort -> sort == SearchParameters.FieldFacetSort.INDEX)
                    .map(sort -> Map.of("_key", SortOrder.Asc)).ifPresent(termsBuilder::order);

            ofNullable(specs.getPrefix()).map(prefix -> new TermsInclude.Builder().terms(Collections.singletonList(prefix + ".*")).build())
                    .ifPresent(termsBuilder::include);

            if (specs.isCountDocsMissingFacetField())
            {
                // retro-compatibility with pre-existing behaviour
                termsBuilder.missing(FieldValue.of("null"));
            }

            ofNullable(specs.getLimitOrNull()).ifPresent(termsBuilder::size);

            return termsBuilder.build();
        });
    }

    public void setDefaultFacetLimit(int defaultFacetLimit)
    {
        this.defaultFacetLimit = defaultFacetLimit;
    }

    public int getDefaultFacetLimit()
    {
        return defaultFacetLimit;
    }

    /**
     * Starting from a facet field name, returns the corresponding Elasticsearch field name. The input facet field can be one of the following:
     *
     * <ul>
     * <li>a basic field (e.g. <code>SITE</code>, <code>OWNER</code>). No namespace is taken in account</li>
     * <li>a content model field without namespace (e.g. <code>modifier</code>, <code>creator</code>). In this case the field is prefixed with the default namespace.</li>
     * <li>a content model field with namespace (e.g. <code>cm:modifier</code>, <code>cm:creator</code>, <code>{http://www.alfresco.org/Fmodel/content/1.0}creator</code>). In this case the field will use the associated namespace.</li>
     * </ul>
     * <p>
     * faceting requires the untokenized version of the field, so once the proper field name is detected among one of the three scenarios above, the {@link FieldMappingBuilder} is asked for the name of the corresponding untokenized field.
     *
     * This method also supports the lucene based syntax (fields prefixed with the <code>@</code>) for the faced field names.
     *
     * @param facet
     *            the Facet specs (in the input request)
     * @return the name of the corresponding field in Elasticsearch.
     */
    private String fieldNameFrom(SearchParameters.FieldFacet facet, SearchParameters searchParameters)
    {
        String fieldName = toPropertyName(facet.getField(), searchParameters.getNamespace());

        boolean hasFullyQualifiedName = isNotEmpty(QName.createQName(fieldName).getNamespaceURI());
        if (hasFullyQualifiedName)
        {
            fieldName = QName.resolveToQName(namespaceDAO, fieldName).toPrefixString(namespaceDAO);
        }

        return FieldName.untokenized(fieldName);
    }

    private String toPropertyName(final String fieldName, final String defaultNamespace)
    {
        final AlfrescoFunctionEvaluationContext functionContext = new AlfrescoFunctionEvaluationContext(namespaceDAO,
                dictionaryService, defaultNamespace);

        final String luceneFieldName = functionContext.getLuceneFieldName(asPropertyName(fieldName));

        return asPropertyName(luceneFieldName);
    }

    private String asPropertyName(final String property)
    {
        return isLuceneSyntaxProperty(property) ? property.substring(1) : property;
    }

    private boolean isLuceneSyntaxProperty(String property)
    {
        return property.startsWith(PROPERTY_FIELD_PREFIX);
    }

    private List<FiltersAggregation> buildFilterAggregations(SearchParameters parameters,
                                                             LanguageQueryBuilder languageQueryBuilder)
    {
        return parameters.getFacetQueries()
                .stream()
                .map(ElasticsearchQueryHelper::extractFacetQueryAndLabel)
                .map(facetQueryAndLabelResult -> facetQueryAndLabelResult.map(facetQueryAndLabel -> {
                            String aftsQuery = facetQueryAndLabel.getFirst();
                            String label = facetQueryAndLabel.getSecond();

                            SearchParameters facetQueryParams = new SearchParameters();
                            facetQueryParams.setQuery(aftsQuery);

                            try
                            {
                                Query elasticsearchQuery = languageQueryBuilder.getQuery(facetQueryParams);
                                Buckets<Query> bucketQuery = new Buckets.Builder<Query>().keyed(Map.of(label, elasticsearchQuery))
                                        .build();
                                return new FiltersAggregation.Builder().filters(bucketQuery)
                                        .build();
                            }
                            catch (ParseException | FTSQueryException e)
                            {
                                LOGGER.warn("Cannot parse AFTS facet query: {}", aftsQuery);
                                return null;
                            }
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

    }
}
