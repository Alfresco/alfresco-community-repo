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
package org.alfresco.repo.search.impl.elasticsearch.resultset;

import static java.util.Optional.ofNullable;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.util.Pair;

public class ElasticsearchResultSetBuilder
{
    private final NodeService nodeService;
    private final NodeDAO nodeDAO;
    private final HighlightsHandler highlightsHandler;
    private final AggregationHandler aggregationHandler;

    public ElasticsearchResultSetBuilder(NodeService nodeService, NodeDAO nodeDAO, HighlightsHandler highlightsHandler,
            AggregationHandler aggregationHandler)
    {
        this.nodeService = nodeService;
        this.nodeDAO = nodeDAO;
        this.highlightsHandler = highlightsHandler;
        this.aggregationHandler = aggregationHandler;
    }

    public ElasticsearchResultSet build(SearchParameters searchParameters, SearchResponse<Object> searchResponse)
    {
        var hits = ofNullable(searchResponse.hits()).map(HitsMetadata::hits).orElse(List.of());
        List<NodeRefAndScore> nodeRefAndScores = mapNodeRefsAndScores(hits, searchParameters.isBulkFetchEnabled());

        var resultSetMetaData = new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, searchParameters);
        var spellCheckResult = new SpellCheckResult(null, null, false);
        long queryTime = searchResponse.took();
        long numFound = searchResponse.hits()
                .total()
                .value();
        int start = searchParameters.getSkipCount();
        Aggregation aggregation = aggregationHandler.handle(searchResponse);
        Map<String, Integer> facetQueries = aggregation.facetQueries();
        Map<String, List<Pair<String, Integer>>> fieldFacets = aggregation.fieldFacets();
        Map<NodeRef, List<Pair<String, List<String>>>> highlights = highlightsHandler.handle(searchParameters, searchResponse);
        return new ElasticsearchResultSet(
                nodeService,
                nodeRefAndScores,
                resultSetMetaData,
                spellCheckResult,
                queryTime,
                numFound,
                start,
                facetQueries,
                fieldFacets,
                highlights);
    }

    public ElasticsearchResultSet build(SearchParameters searchParameters, List<Hit<Object>> hits, long totalHits, long queryTime)
    {
        List<NodeRefAndScore> nodeRefAndScores = mapNodeRefsAndScores(hits, searchParameters.isBulkFetchEnabled());

        var resultSetMetaData = new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, searchParameters);
        var spellCheckResult = new SpellCheckResult(null, null, false);
        int start = searchParameters.getSkipCount();

        // Aggregations and highlights are not available in scroll responses
        return new ElasticsearchResultSet(
                nodeService,
                nodeRefAndScores,
                resultSetMetaData,
                spellCheckResult,
                queryTime,
                totalHits,
                start,
                Map.of(),
                Map.of(),
                Map.of());
    }

    private List<NodeRefAndScore> mapNodeRefsAndScores(List<Hit<Object>> hits, boolean isBulkFetchEnabled)
    {
        cacheNodes(hits.stream().map(hit -> new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, hit.id())).toList(), isBulkFetchEnabled);
        List<NodeRefAndScore> results = new ArrayList<>();
        for (Hit<Object> hit : hits)
        {
            NodeRef nodeRef = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, hit.id());
            if (nodeService.exists(nodeRef))
            {
                results.add(new NodeRefAndScore(nodeRef, hit.score().floatValue()));
            }
        }
        return results;
    }

    private void cacheNodes(List<NodeRef> nodesRef, boolean isBulkFetchEnabled)
    {
        // bulk load
        if (isBulkFetchEnabled)
        {
            nodeDAO.setCheckNodeConsistency();
            nodeDAO.cacheNodes(nodesRef);
        }
    }
}
