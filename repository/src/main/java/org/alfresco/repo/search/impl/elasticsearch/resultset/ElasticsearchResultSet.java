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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.SearchEngineResultSet;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.Metric;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.*;
import org.alfresco.util.Pair;

/**
 * The {@link ResultSet} implementation for Elasticsearch subsystem
 */
public class ElasticsearchResultSet implements SearchEngineResultSet
{
    private final NodeService nodeService;

    private final List<NodeRefAndScore> nodeRefAndScores;

    private final SimpleResultSetMetaData resultSetMetaData;

    private final SpellCheckResult spellCheckResult;

    private final long queryTime;

    private final long numFound;

    private final int start;

    private final Map<String, Integer> facetQueries;
    private final Map<String, List<Pair<String, Integer>>> fieldFacets;
    private final Map<NodeRef, List<Pair<String, List<String>>>> highlights;

    public ElasticsearchResultSet(NodeService nodeService, List<NodeRefAndScore> nodeRefAndScores, SimpleResultSetMetaData resultSetMetaData,
            SpellCheckResult spellCheckResult, long queryTime, long numFound, int start,
            Map<String, Integer> facetQueries, Map<String, List<Pair<String, Integer>>> fieldFacets,
            Map<NodeRef, List<Pair<String, List<String>>>> highlights)
    {
        this.nodeService = nodeService;
        this.nodeRefAndScores = nodeRefAndScores;
        this.resultSetMetaData = resultSetMetaData;
        this.spellCheckResult = spellCheckResult;
        this.queryTime = queryTime;
        this.numFound = numFound;
        this.start = start;
        this.facetQueries = facetQueries;
        this.fieldFacets = fieldFacets;
        this.highlights = highlights;
    }

    @Override
    public int length()
    {
        return nodeRefAndScores.size();
    }

    @Override
    public long getNumberFound()
    {
        return numFound;
    }

    @Override
    public NodeRef getNodeRef(int n)
    {
        return nodeRefAndScores.get(n).nodeRef();
    }

    @Override
    public float getScore(int i)
    {
        return nodeRefAndScores.get(i).score();
    }

    @Override
    public void close()
    {
        // NO OP
    }

    @Override
    public ResultSetRow getRow(int i)
    {
        return new ElasticsearchResultSetRow(this, i);
    }

    @Override
    public List<NodeRef> getNodeRefs()
    {
        return nodeRefAndScores.stream()
                .map(NodeRefAndScore::nodeRef)
                .toList();
    }

    @Override
    public List<ChildAssociationRef> getChildAssocRefs()
    {
        return nodeRefAndScores.stream()
                .map(NodeRefAndScore::nodeRef)
                .map(nodeService::getPrimaryParent)
                .toList();
    }

    @Override
    public ChildAssociationRef getChildAssocRef(int n)
    {
        return nodeService.getPrimaryParent(getNodeRef(n));
    }

    @Override
    public ResultSetMetaData getResultSetMetaData()
    {
        return resultSetMetaData;
    }

    @Override
    public int getStart()
    {
        return start;
    }

    @Override
    public boolean hasMore()
    {
        return getNumberFound() > (getStart() + length());
    }

    @Override
    public boolean setBulkFetch(boolean bulkFetch)
    {
        return bulkFetch;
    }

    @Override
    public boolean getBulkFetch()
    {
        return true;
    }

    @Override
    public int setBulkFetchSize(int bulkFetchSize)
    {
        return bulkFetchSize;
    }

    @Override
    public int getBulkFetchSize()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public List<Pair<String, Integer>> getFieldFacet(String facetLabelOrName)
    {
        return fieldFacets.getOrDefault(facetLabelOrName, emptyList());
    }

    @Override
    public Map<NodeRef, List<Pair<String, List<String>>>> getHighlighting()
    {
        return highlights;
    }

    @Override
    public SpellCheckResult getSpellCheckResult()
    {
        return spellCheckResult;
    }

    @Override
    public Iterator<ResultSetRow> iterator()
    {
        return new ElasticsearchResultSetRowIterator(this);
    }

    @Override
    public Long getQueryTime()
    {
        return queryTime;
    }

    @Override
    public Map<String, Integer> getFacetQueries()
    {
        return facetQueries;
    }

    @Override
    public Map<String, List<Pair<String, Integer>>> getFieldFacets()
    {
        return fieldFacets;
    }

    @Override
    public Map<String, List<Pair<String, Integer>>> getFacetIntervals()
    {
        return emptyMap();
    }

    @Override
    public Map<String, List<Map<String, String>>> getFacetRanges()
    {
        return emptyMap();
    }

    @Override
    public List<GenericFacetResponse> getPivotFacets()
    {
        return emptyList();
    }

    @Override
    public Map<String, Set<Metric>> getStats()
    {
        return emptyMap();
    }

    @Override
    public long getLastIndexedTxId()
    {
        return -1;
    }

    @Override
    public boolean getProcessedDenies()
    {
        return true;
    }

    NodeService getNodeService()
    {
        return nodeService;
    }
}
