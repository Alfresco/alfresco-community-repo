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
package org.alfresco.repo.search.impl.elasticsearch.query.aggregation;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SITE;
import static org.alfresco.repo.search.adaptor.QueryConstants.PROPERTY_FIELD_PREFIX;
import static org.alfresco.repo.search.impl.elasticsearch.util.CollectionUtils.safe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.TermsAggregationWrapper.ComplementaryAggregation;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.repo.search.impl.parsers.FTSQueryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Build the filter and terms aggregation starting from the search parameters and using the specified query language.
 */
public class ElasticsearchAggregationBuilder
{

    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";

    // The primary hierarchy field name in Elasticsearch, used to resolve SITE aggregations.
    private static final String PRIMARY_HIERARCHY_FIELD = "primaryHierarchy";

    // Label for special SHARED facet buckets
    private static final String SHARED_FILES_LABEL = "_SHARED_FILES_";

    // Label for special REPOSITORY facet buckets
    private static final String REPOSITORY_LABEL = "_REPOSITORY_";

    // Suffix for the REPOSITORY complementary filter aggregation. The full aggregation name is {termsAggName} + REPOSITORY_SUFFIX
    private static final String REPOSITORY_SUFFIX = "__REPOSITORY__";

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAggregationBuilder.class);

    private final NamespacePrefixResolver namespaceDAO;
    private final DictionaryService dictionaryService;
    private final SiteService siteService;
    private final NodeService nodeService;
    private int defaultFacetLimit;
    private volatile NodeRef sharedHomeNodeRef;

    public ElasticsearchAggregationBuilder(NamespaceDAO namespaceDAO, DictionaryService dictionaryService, SiteService siteService, NodeService nodeService)
    {
        this.namespaceDAO = namespaceDAO;
        this.dictionaryService = dictionaryService;
        this.siteService = siteService;
        this.nodeService = nodeService;
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
     * Builds terms aggregations from the given search parameters. SITE facets are handled specially: they are translated to <code>primaryHierarchy</code> aggregations with an <code>include</code> list of site node UUIDs (visible to the currently authenticated user) plus the Shared Files folder UUID. The returned {@link TermsAggregationWrapper} carries:
     * <ul>
     * <li>a UUID-to-name-label map so the result set can translate bucket keys back to site short names or {@code _SHARED_FILES_}</li>
     * <li>a {@link ComplementaryAggregator} filter aggregation that counts documents not in any site or Shared Files, to be presented as {@code _REPOSITORY_} bucket</li>
     * </ul>
     *
     * @param parameters
     *            search parameters containing field facet specs
     * @param languageQueryBuilder
     *            the language query builder used to build the terms aggregations query
     * @return stream of Alfresco terms aggregations
     */
    public Stream<TermsAggregationWrapper> termsAggregations(SearchParameters parameters,
            LanguageQueryBuilder languageQueryBuilder)
    {
        return safe(parameters.getFieldFacets()).stream().flatMap(specs -> {

            if (isSiteFacet(specs))
            {
                return buildSiteTermsAggregation(specs).stream();
            }

            String aggregationName = ofNullable(specs.getLabel()).orElse(specs.getField());

            final TermsAggregation.Builder termsBuilder = AggregationBuilders.terms()
                    .name(aggregationName)
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

            return Stream.of(new TermsAggregationWrapper(aggregationName, termsBuilder.build(), emptyMap(), empty()));
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
     * Detects whether the given facet spec targets the SITE field.
     */
    private boolean isSiteFacet(SearchParameters.FieldFacet facet)
    {
        return FIELD_SITE.equals(asPropertyName(facet.getField()));
    }

    /**
     * Builds a {@link TermsAggregationWrapper} for a SITE facet.
     * <p>
     * Calls {@link SiteService#listSites(String, String)} to obtain the visible sites to the authenticated user, then constructs a {@code primaryHierarchy} terms aggregation whose {@code include} list is limited to accessible site node UUIDs plus the Shared Files folder UUID.
     * <p>
     * A repository filter aggregation is also produced to count documents that do not belong to any of the included site UUIDs; this will be presented as a {@code _REPOSITORY_} bucket in the results.
     * <p>
     * Returns an empty Optional if no sites are visible and the Shared Files folder cannot be resolved.
     */
    private Optional<TermsAggregationWrapper> buildSiteTermsAggregation(SearchParameters.FieldFacet specs)
    {
        List<SiteInfo> sites = siteService.listSites(null, null);

        Map<String, String> siteIdToName = new LinkedHashMap<>();

        // Collect site UUID and corresponding short name entries
        sites.stream()
                .filter(site -> site.getNodeRef() != null)
                .forEach(site -> siteIdToName.put(site.getNodeRef().getId(), site.getShortName()));

        // Add Shared Files folder UUID (_SHARED_FILES_)
        Optional<NodeRef> sharedHome = getSharedHomeNodeRef();
        sharedHome.ifPresent(nodeRef -> siteIdToName.put(nodeRef.getId(), SHARED_FILES_LABEL));

        if (siteIdToName.isEmpty())
        {
            LOGGER.debug("No sites visible and Shared Files folder not resolved; skipping SITE terms aggregation.");
            return Optional.empty();
        }

        List<String> includeUUIDs = new ArrayList<>(siteIdToName.keySet());

        String aggregationName = ofNullable(specs.getLabel()).orElse(specs.getField());

        final TermsAggregation.Builder termsBuilder = AggregationBuilders.terms()
                .name(aggregationName)
                .field(PRIMARY_HIERARCHY_FIELD)
                .include(new TermsInclude.Builder().terms(includeUUIDs).build())
                .minDocCount(specs.getMinCount())
                .size(includeUUIDs.size());

        ofNullable(specs.getSort()).filter(sort -> sort == SearchParameters.FieldFacetSort.INDEX)
                .map(sort -> Map.of("_key", SortOrder.Asc)).ifPresent(termsBuilder::order);

        if (specs.isCountDocsMissingFacetField())
        {
            termsBuilder.missing(FieldValue.of("null"));
        }

        ComplementaryAggregation repositoryAggregation = buildRepositoryAggregation(aggregationName, siteIdToName);

        return Optional.of(new TermsAggregationWrapper(
                aggregationName,
                termsBuilder.build(),
                Collections.unmodifiableMap(siteIdToName),
                Optional.of(repositoryAggregation)));
    }

    /**
     * Builds a ComplementaryAggregation to be used together with a SITE terms aggregation, to count documents that do not belong to any of the included site UUIDs (i.e. documents in the repository but not in any site).
     * 
     * @param termsAggName
     *            the name of the main terms aggregation, used to derive the name of the complementary aggregation
     * @param siteIdToName
     *            a map of site UUIDs to their corresponding names
     * @return a ComplementaryAggregation for the repository
     */
    private ComplementaryAggregation buildRepositoryAggregation(String termsAggName, Map<String, String> siteIdToName)
    {
        List<FieldValue> allUUIDValues = siteIdToName.keySet().stream().map(FieldValue::of).toList();

        Query repositoryFilterQuery = Query.of(q -> q
                .bool(b -> b
                        .mustNot(mn -> mn
                                .terms(t -> t
                                        .field(PRIMARY_HIERARCHY_FIELD)
                                        .terms(tv -> tv.value(allUUIDValues))))));

        String repositoryAggregationName = termsAggName + REPOSITORY_SUFFIX;
        return new ComplementaryAggregation(repositoryAggregationName, repositoryFilterQuery, REPOSITORY_LABEL);
    }

    /**
     * Resolves the NodeRef for the Shared Files folder ({@code /app:company_home/app:shared}). The result is cached after the first successful resolution.
     *
     * @return the NodeRef, or {@code null} if it cannot be resolved
     */
    private Optional<NodeRef> getSharedHomeNodeRef()
    {
        if (sharedHomeNodeRef != null)
        {
            LOGGER.debug("Shared Files NodeRef already resolved: {}", sharedHomeNodeRef);
            return Optional.of(sharedHomeNodeRef);
        }

        NodeRef root = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        List<ChildAssociationRef> companyHomeAssocs = nodeService.getChildAssocs(root,
                RegexQNamePattern.MATCH_ALL,
                QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home"));

        if (companyHomeAssocs.isEmpty())
        {
            LOGGER.warn("Could not find company home node; {} bucket will not be available.", SHARED_FILES_LABEL);
            return Optional.empty();
        }

        NodeRef companyHome = companyHomeAssocs.get(0).getChildRef();

        List<ChildAssociationRef> sharedAssocs = nodeService.getChildAssocs(companyHome,
                RegexQNamePattern.MATCH_ALL,
                QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "shared"));

        if (sharedAssocs.isEmpty())
        {
            LOGGER.warn("Could not find Shared Files folder; {} bucket will not be available.", SHARED_FILES_LABEL);
            return Optional.empty();
        }

        sharedHomeNodeRef = sharedAssocs.get(0).getChildRef();
        LOGGER.debug("Shared Files NodeRef resolved: {}", sharedHomeNodeRef);
        return Optional.of(sharedHomeNodeRef);
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
