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

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.aggregations.AggregationBuilders;
import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;
import org.opensearch.client.opensearch._types.aggregations.TermsInclude;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.TermsAggregationWrapper.ComplementaryAggregation;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class SiteTermsAggregationBuilder
{
    // The primary hierarchy field name in Elasticsearch, used to resolve SITE aggregations.
    private static final String PRIMARY_HIERARCHY_FIELD = "primaryHierarchy";

    // Label for special SHARED facet buckets
    private static final String SHARED_FILES_LABEL = "_SHARED_FILES_";

    // Label for special REPOSITORY facet buckets
    private static final String REPOSITORY_LABEL = "_REPOSITORY_";

    // Suffix for the REPOSITORY complementary filter aggregation. The full aggregation name is {termsAggName} + REPOSITORY_SUFFIX
    private static final String REPOSITORY_SUFFIX = "__REPOSITORY__";

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteTermsAggregationBuilder.class);

    private final SiteService siteService;
    private final NodeService nodeService;
    private volatile NodeRef sharedHomeNodeRef;

    public SiteTermsAggregationBuilder(SiteService siteService, NodeService nodeService)
    {
        this.siteService = siteService;
        this.nodeService = nodeService;
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
    public Optional<TermsAggregationWrapper> build(SearchParameters.FieldFacet specs)
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
     * @return an {@link Optional} containing the NodeRef, or {@link Optional#empty()} if it cannot be resolved
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
}
