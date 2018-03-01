/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.rest.api.search.impl;

import static org.alfresco.rest.api.search.impl.StoreMapper.DELETED;
import static org.alfresco.rest.api.search.impl.StoreMapper.HISTORY;
import static org.alfresco.rest.api.search.impl.StoreMapper.LIVE_NODES;
import static org.alfresco.rest.api.search.impl.StoreMapper.VERSIONS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericBucket;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse.FACET_TYPE;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.Metric;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.Metric.METRIC_TYPE;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.RangeResultMapper;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.SimpleMetric;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.rest.api.DeletedNodes;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.lookups.PropertyLookupRegistry;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.api.nodes.NodeVersionsRelation;
import org.alfresco.rest.api.search.context.FacetFieldContext;
import org.alfresco.rest.api.search.context.FacetFieldContext.Bucket;
import org.alfresco.rest.api.search.context.FacetQueryContext;
import org.alfresco.rest.api.search.context.SearchContext;
import org.alfresco.rest.api.search.context.SearchRequestContext;
import org.alfresco.rest.api.search.context.SpellCheckContext;
import org.alfresco.rest.api.search.model.FacetField;
import org.alfresco.rest.api.search.model.FacetQuery;
import org.alfresco.rest.api.search.model.HighlightEntry;
import org.alfresco.rest.api.search.model.SearchEntry;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.api.search.model.SearchSQLQuery;
import org.alfresco.rest.api.search.model.TupleEntry;
import org.alfresco.rest.api.search.model.TupleList;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.FacetFormat;
import org.alfresco.service.cmr.search.Interval;
import org.alfresco.service.cmr.search.IntervalSet;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Maps from a ResultSet to a json public api representation.
 *
 * @author Gethin James
 */
public class ResultMapper
{
    private ServiceRegistry serviceRegistry;
    private Nodes nodes;
    private NodeVersionsRelation nodeVersions;
    private PropertyLookupRegistry propertyLookup;
    private StoreMapper storeMapper;
    private DeletedNodes deletedNodes;
    private static Log logger = LogFactory.getLog(ResultMapper.class);

    public ResultMapper()
    {
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setNodeVersions(NodeVersionsRelation nodeVersions)
    {
        this.nodeVersions = nodeVersions;
    }

    public void setDeletedNodes(DeletedNodes deletedNodes)
    {
        this.deletedNodes = deletedNodes;
    }

    public void setStoreMapper(StoreMapper storeMapper)
    {
        this.storeMapper = storeMapper;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setPropertyLookup(PropertyLookupRegistry propertyLookup)
    {
        this.propertyLookup = propertyLookup;
    }

    /**
     * Turns the results into a CollectionWithPagingInfo
     * @param params
     * @param searchQuery
     *@param results  @return CollectionWithPagingInfo<Node>
     */
    public CollectionWithPagingInfo<Node> toCollectionWithPagingInfo(Params params, SearchRequestContext searchRequestContext, SearchQuery searchQuery, ResultSet results)
    {
        SearchContext context = null;
        Integer total = null;
        List<Node> noderesults = new ArrayList<Node>();
        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);
        Map<NodeRef, List<Pair<String, List<String>>>> hightLighting = results.getHighlighting();
        int notFound = 0;
        boolean isHistory = searchRequestContext.getStores().contains(StoreMapper.HISTORY);

        for (ResultSetRow row:results)
        {
            Node aNode = getNode(row, params, mapUserInfo, isHistory);

            if (aNode != null)
            {
                float f = row.getScore();
                List<HighlightEntry> highlightEntries = null;
                List<Pair<String, List<String>>> high = hightLighting.get(row.getNodeRef());

                if (high != null && !high.isEmpty())
                {
                    highlightEntries = new ArrayList<HighlightEntry>(high.size());
                    for (Pair<String, List<String>> highlight:high)
                    {
                        highlightEntries.add(new HighlightEntry(highlight.getFirst(), highlight.getSecond()));
                    }
                }
                aNode.setSearch(new SearchEntry(f, highlightEntries));
                noderesults.add(aNode);
            }
            else
            {
                logger.debug("Unknown noderef returned from search results "+row.getNodeRef());
                notFound++;
            }
        }

        SolrJSONResultSet solrResultSet = findSolrResultSet(results);

        if (solrResultSet != null)
        {
            //We used Solr for this query
            context = toSearchContext(solrResultSet, searchRequestContext, searchQuery, notFound);
            total = setTotal(solrResultSet);
        }
        else
        {
            //This probably wasn't solr
            if (!results.hasMore())
            {
                //If there are no more results then we are confident that the number found is correct
                //otherwise we are not confident enough that its accurate
                total = setTotal(results);
            }
        }

        return CollectionWithPagingInfo.asPaged(params.getPaging(), noderesults, results.hasMore(), total, null, context);
    }

    /**
     * Builds a node representation based on a ResultSetRow;
     * @param searchRequestContext
     * @param aRow
     * @param params
     * @param mapUserInfo
     * @param isHistory
     * @return Node
     */
    public Node getNode(ResultSetRow aRow, Params params, Map<String, UserInfo> mapUserInfo, boolean isHistory)
    {
        String nodeStore = storeMapper.getStore(aRow.getNodeRef());
        if (isHistory) nodeStore = HISTORY;
        Node aNode = null;

        switch (nodeStore)
        {
            case LIVE_NODES:
                aNode = nodes.getFolderOrDocument(aRow.getNodeRef(), null, null, params.getInclude(), mapUserInfo);
                break;
            case HISTORY:
                aNode = nodes.getFolderOrDocument(aRow.getNodeRef(), null, null, params.getInclude(), mapUserInfo);
                break;
            case VERSIONS:
                Map<QName, Serializable> properties = serviceRegistry.getNodeService().getProperties(aRow.getNodeRef());
                NodeRef frozenNodeRef = ((NodeRef) properties.get(Version2Model.PROP_QNAME_FROZEN_NODE_REF));
                String versionLabelId = (String) properties.get(Version2Model.PROP_QNAME_VERSION_LABEL);
                Version v = null;
                try
                {
                    if (frozenNodeRef != null && versionLabelId != null)
                    {
                        v = nodeVersions.findVersion(frozenNodeRef.getId(),versionLabelId);
                        aNode = nodes.getFolderOrDocument(v.getFrozenStateNodeRef(), null, null, params.getInclude(), mapUserInfo);
                    }
                }
                catch (EntityNotFoundException|InvalidNodeRefException e)
                {
                    //Solr says there is a node but we can't find it
                    logger.debug("Failed to find a versioned node with id of "+frozenNodeRef
                                + " this is probably because the original node has been deleted.");
                }

                if (v != null && aNode != null)
                {
                    nodeVersions.mapVersionInfo(v, aNode, aRow.getNodeRef());
                    aNode.setNodeId(frozenNodeRef.getId());
                    aNode.setVersionLabel(versionLabelId);
                }
                break;
            case DELETED:
                try
                {
                    aNode = deletedNodes.getDeletedNode(aRow.getNodeRef().getId(), params, false, mapUserInfo);
                }
                catch (EntityNotFoundException enfe)
                {
                    //Solr says there is a deleted node but we can't find it, we want the rest of the search to return so lets ignore it.
                    logger.debug("Failed to find a deleted node with id of "+aRow.getNodeRef().getId());
                }
                break;
        }
        if (aNode != null)
        {
            aNode.setLocation(nodeStore);
        }
        return aNode;
    }

    /**
     * Sets the total number found.
     * @param results
     * @return An integer total
     */
    public Integer setTotal(ResultSet results)
    {
        Long totalItems = results.getNumberFound();
        Integer total = totalItems.intValue();
        return total;
    }

    /**
     * Uses the results from Solr to set the Search Context
     * @param SolrJSONResultSet
     * @param searchQuery
     * @return SearchContext
     */
    public SearchContext toSearchContext(SolrJSONResultSet solrResultSet, SearchRequestContext searchRequestContext, SearchQuery searchQuery, int notFound)
    {
        SearchContext context = null;
        Map<String, Integer> facetQueries = solrResultSet.getFacetQueries();
        List<GenericFacetResponse> facets = new ArrayList<>();
        List<FacetQueryContext> facetResults = null;
        SpellCheckContext spellCheckContext = null;
        List<FacetFieldContext> ffcs = new ArrayList<FacetFieldContext>();

        //Facet queries
        if(facetQueries!= null && !facetQueries.isEmpty())
        {
            //If group by field populated in query facet return bucketing into facet field.
            List<GenericFacetResponse> facetQueryForFields = getFacetBucketsFromFacetQueries(facetQueries,searchQuery);
            if(hasGroup(searchQuery) || FacetFormat.V2 == searchQuery.getFacetFormat())
            {
                facets.addAll(facetQueryForFields);
            }
            else
            {
                // Return the old way facet query with no bucketing.
                facetResults = new ArrayList<>(facetQueries.size());
                for (Entry<String, Integer> fq:facetQueries.entrySet())
                {
                    String filterQuery = null;
                    if (searchQuery != null && searchQuery.getFacetQueries() != null)
                    {
                        Optional<FacetQuery> found = searchQuery.getFacetQueries().stream().filter(facetQuery -> fq.getKey().equals(facetQuery.getLabel())).findFirst();
                        filterQuery = found.isPresent()? found.get().getQuery():fq.getKey();
                    }
                    facetResults.add(new FacetQueryContext(fq.getKey(), filterQuery, fq.getValue()));
                }
            }
        }

        //Field Facets
        Map<String, List<Pair<String, Integer>>> facetFields = solrResultSet.getFieldFacets();
        if(FacetFormat.V2 == searchQuery.getFacetFormat())
        {
            facets.addAll(getFacetBucketsForFacetFieldsAsFacets(facetFields, searchQuery));
        }
        else
        {
            ffcs.addAll(getFacetBucketsForFacetFields(facetFields, searchQuery));
        }

        Map<String, List<Pair<String, Integer>>> facetInterval = solrResultSet.getFacetIntervals();
        facets.addAll(getGenericFacetsForIntervals(facetInterval, searchQuery));
        
        Map<String,List<Map<String,String>>> facetRanges = solrResultSet.getFacetRanges();
        facets.addAll(RangeResultMapper.getGenericFacetsForRanges(facetRanges, searchQuery.getFacetRanges()));

        List<GenericFacetResponse> stats = getFieldStats(searchRequestContext, solrResultSet.getStats());
        List<GenericFacetResponse> pimped = getPivots(searchRequestContext, solrResultSet.getPivotFacets(), stats);
        facets.addAll(pimped);
        facets.addAll(stats);

        //Spelling
        SpellCheckResult spell = solrResultSet.getSpellCheckResult();
        if (spell != null && spell.getResultName() != null && !spell.getResults().isEmpty())
        {
            spellCheckContext = new SpellCheckContext(spell.getResultName(),spell.getResults());
        }

        //Put it all together
        context = new SearchContext(solrResultSet.getLastIndexedTxId(), facets, facetResults, ffcs, spellCheckContext, searchRequestContext.includeRequest()?searchQuery:null);
        return isNullContext(context)?null:context;
    }
    public static boolean hasGroup(SearchQuery searchQuery)
    {
        if(searchQuery != null && searchQuery.getFacetQueries() != null)
        {
            return searchQuery.getFacetQueries().stream().anyMatch(facetQ -> facetQ.getGroup() != null);
        }
        return false;
    }
    /**
     * Builds a facet field from facet queries.
     * @param facetQueries
     * @return
     */
    protected List<GenericFacetResponse> getFacetBucketsFromFacetQueries(Map<String, Integer> facetQueries, SearchQuery searchQuery)
    {
        List<GenericFacetResponse> facetResults = new ArrayList<GenericFacetResponse>();
        Map<String,List<GenericBucket>> groups = new HashMap<>();
        
        for (Entry<String, Integer> fq:facetQueries.entrySet())
        {
            String group = null;
            String filterQuery = null;
            if (searchQuery != null && searchQuery.getFacetQueries() != null)
            {
                Optional<FacetQuery> found = searchQuery.getFacetQueries().stream().filter(facetQuery -> fq.getKey().equals(facetQuery.getLabel())).findFirst();
                filterQuery = found.isPresent()? found.get().getQuery():fq.getKey();
                if(found.isPresent() && found.get().getGroup() != null)
                {
                    group = found.get().getGroup();
                }
            }
//            if(group != null && !group.isEmpty() || FacetFormat.V2 == searchQuery.getFacetFormat())
//            {
                if(groups.containsKey(group)) 
                {
                    Set<Metric> metrics = new HashSet<>(1);
                    metrics.add(new SimpleMetric(METRIC_TYPE.count, fq.getValue()));
                    groups.get(group).add(new GenericBucket(fq.getKey(), filterQuery, null,metrics, null));
                }
                else
                {
                    List<GenericBucket> l = new ArrayList<GenericBucket>();
                    Set<Metric> metrics = new HashSet<>(1);
                    metrics.add(new SimpleMetric(METRIC_TYPE.count, fq.getValue()));
                    l.add(new GenericBucket(fq.getKey(),filterQuery, null, metrics, null));
                    groups.put(group, l);
                }
            }
//        }
        if(!groups.isEmpty())
        {
            groups.forEach((a,v) -> facetResults.add(new GenericFacetResponse(FACET_TYPE.query, a, v)));
        }
        return facetResults;
    }

    protected List<GenericFacetResponse> getFieldStats(SearchRequestContext searchRequestContext, Map<String, Set<Metric>> stats)
    {
        if(stats != null && !stats.isEmpty())
        {
            return stats.entrySet().stream().map(statsFieldEntry -> {
               return new GenericFacetResponse(FACET_TYPE.stats, statsFieldEntry.getKey(),
                           Arrays.asList(new GenericBucket(null,null, null,
                                       statsFieldEntry.getValue(), null)) );
            }
            ).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    protected List<GenericFacetResponse> getPivots(SearchRequestContext searchRequest, List<GenericFacetResponse> pivots,
                List<GenericFacetResponse> stats)
    {
        if(pivots != null && !pivots.isEmpty())
        {
            Map<String, String> pivotKeys = searchRequest.getPivotKeys();

            return pivots.stream().map(aFacet -> {

                String pivotLabel = pivotKeys.containsKey(aFacet.getLabel())?pivotKeys.get(aFacet.getLabel()):aFacet.getLabel();

                //can reference, facetfield, the last one can be rangefacet, facetquery or stats
                List<GenericBucket> bucks = new ArrayList<>();
                Optional<GenericFacetResponse> foundStat = stats.stream().filter(
                            aStat -> aStat.getLabel().equals(pivotLabel)).findFirst();
                if (foundStat.isPresent())
                {
                   bucks.add(foundStat.get().getBuckets().get(0));
                   stats.remove(foundStat.get());
                }
                bucks.addAll(aFacet.getBuckets().stream().map(genericBucket -> {
                    Object display = propertyLookup.lookup(aFacet.getLabel(), genericBucket.getLabel());
                    return new GenericBucket(genericBucket.getLabel(), genericBucket.getFilterQuery(),
                                display,genericBucket.getMetrics(), getPivots(searchRequest, genericBucket.getFacets(), stats));
                }).collect(Collectors.toList()));

                return new GenericFacetResponse(aFacet.getType(), pivotLabel, bucks);
            }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
    protected List<GenericFacetResponse> getFacetBucketsForFacetFieldsAsFacets(Map<String, List<Pair<String, Integer>>> facetFields, SearchQuery searchQuery)
    {
        if (facetFields != null && !facetFields.isEmpty())
        {
            List<GenericFacetResponse> ffcs = new ArrayList<>(facetFields.size());
            for (Entry<String, List<Pair<String, Integer>>> facet:facetFields.entrySet())
            {
                if (facet.getValue() != null && !facet.getValue().isEmpty())
                {
                    List<GenericBucket> buckets = new ArrayList<>(facet.getValue().size());
                    for (Pair<String, Integer> buck:facet.getValue())
                    {
                        Object display = null;
                        String filterQuery = null;
                        if (searchQuery != null
                                    && searchQuery.getFacetFields() != null
                                    && searchQuery.getFacetFields().getFacets() != null
                                    && !searchQuery.getFacetFields().getFacets().isEmpty())
                        {
                            Optional<FacetField> found = searchQuery.getFacetFields().getFacets().stream().filter(
                                        queryable -> facet.getKey().equals(queryable.getLabel()!=null?queryable.getLabel():queryable.getField())).findFirst();
                            if (found.isPresent())
                            {
                                display = propertyLookup.lookup(found.get().getField(), buck.getFirst());
                                String fq = found.get().toFilterQuery(buck.getFirst());
                                if (fq != null)
                                {
                                    filterQuery = fq;
                                }
                            }
                        }
                        GenericBucket bucket = new GenericBucket(buck.getFirst(), filterQuery, display, new HashSet<Metric>(Arrays.asList(new SimpleMetric(METRIC_TYPE.count,String.valueOf(buck.getSecond())))), null, null);
                        buckets.add(bucket);
                    }
                    ffcs.add(new GenericFacetResponse(FACET_TYPE.field,facet.getKey(), buckets));
                }
            }
            return ffcs;
        }
        return Collections.emptyList();
    }
    protected List<FacetFieldContext> getFacetBucketsForFacetFields(Map<String, List<Pair<String, Integer>>> facetFields, SearchQuery searchQuery)
    {
        if (facetFields != null && !facetFields.isEmpty())
        {
            List<FacetFieldContext> ffcs = new ArrayList<>(facetFields.size());
            for (Entry<String, List<Pair<String, Integer>>> facet:facetFields.entrySet())
            {
                if (facet.getValue() != null && !facet.getValue().isEmpty())
                {
                    List<Bucket> buckets = new ArrayList<>(facet.getValue().size());
                    for (Pair<String, Integer> buck:facet.getValue())
                    {
                        Object display = null;
                        String filterQuery = null;
                        if (searchQuery != null
                                    && searchQuery.getFacetFields() != null
                                    && searchQuery.getFacetFields().getFacets() != null
                                    && !searchQuery.getFacetFields().getFacets().isEmpty())
                        {
                            Optional<FacetField> found = searchQuery.getFacetFields().getFacets().stream().filter(
                                        queryable -> facet.getKey().equals(queryable.getLabel()!=null?queryable.getLabel():queryable.getField())).findFirst();
                            if (found.isPresent())
                            {
                                display = propertyLookup.lookup(found.get().getField(), buck.getFirst());
                                String fq = found.get().toFilterQuery(buck.getFirst());
                                if (fq != null)
                                {
                                    filterQuery = fq;
                                }
                            }
                        }
                        buckets.add(new Bucket(buck.getFirst(), filterQuery,buck.getSecond(),display));
                    }
                    ffcs.add(new FacetFieldContext(facet.getKey(), buckets));
                }
            }

            return ffcs;
        }
        return Collections.emptyList();
    }
    /**
     * Returns generic faceting responses for Intervals
     * @param facetFields
     * @param searchQuery
     * @return GenericFacetResponse
     */
    protected static List<GenericFacetResponse> getGenericFacetsForIntervals(Map<String, List<Pair<String, Integer>>> facetFields, SearchQuery searchQuery)
    {
        if (facetFields != null && !facetFields.isEmpty())
        {
            List<GenericFacetResponse> ffcs = new ArrayList<>(facetFields.size());
            for (Entry<String, List<Pair<String, Integer>>> facet:facetFields.entrySet())
            {
                if (facet.getValue() != null && !facet.getValue().isEmpty())
                {
                    List<GenericBucket> buckets = new ArrayList<>(facet.getValue().size());
                    for (Pair<String, Integer> buck:facet.getValue())
                    {
                        String filterQuery = null;
                        Map<String, String> bucketInfo = new HashMap<>();

                        if (searchQuery != null
                                    && searchQuery.getFacetIntervals() != null
                                    && searchQuery.getFacetIntervals().getIntervals() != null
                                    && !searchQuery.getFacetIntervals().getIntervals().isEmpty())
                        {
                            Optional<Interval> found = searchQuery.getFacetIntervals().getIntervals().stream().filter(
                                        interval -> facet.getKey().equals(interval.getLabel()!=null?interval.getLabel():interval.getField())).findFirst();
                            if (found.isPresent())
                            {
                                if (found.get().getSets() != null)
                                {
                                    Optional<IntervalSet> foundSet = found.get().getSets().stream().filter(aSet -> buck.getFirst().equals(aSet.getLabel())).findFirst();
                                    if (foundSet.isPresent())
                                    {
                                        filterQuery = found.get().getField() + ":" + foundSet.get().toAFTSQuery();
                                        bucketInfo.put(GenericFacetResponse.START, foundSet.get().getStart());
                                        bucketInfo.put(GenericFacetResponse.END, foundSet.get().getEnd());
                                        bucketInfo.put(GenericFacetResponse.START_INC, String.valueOf(foundSet.get().isStartInclusive()));
                                        bucketInfo.put(GenericFacetResponse.END_INC, String.valueOf(foundSet.get().isEndInclusive()));
                                    }
                                }
                            }
                        }
                        GenericBucket bucket = new GenericBucket(buck.getFirst(), filterQuery, null , new HashSet<Metric>(Arrays.asList(new SimpleMetric(METRIC_TYPE.count,String.valueOf(buck.getSecond())))), null, bucketInfo);
                        buckets.add(bucket);
                    }
                    ffcs.add(new GenericFacetResponse(FACET_TYPE.interval, facet.getKey(), buckets));
                }
            }

            return ffcs;
        }
        return Collections.emptyList();
    }

    /**
     * Is the context null?
     * @param context
     * @return true if its null
     */
    public boolean isNullContext(SearchContext context)
    {
        return (context.getFacetQueries() == null
                    && context.getConsistency() == null
                    && context.getSpellCheck() == null
                    && context.getFacetsFields() == null
                    && context.getFacets() == null);
    }

    /**
     * Gets SolrJSONResultSet class if there is one.
     * @param results
     * @return
     */
    protected SolrJSONResultSet findSolrResultSet(ResultSet results)
    {
        ResultSet theResultSet = results;

        if (results instanceof FilteringResultSet)
        {
            theResultSet = ((FilteringResultSet) results).getUnFilteredResultSet();
        }

        if (theResultSet instanceof SolrJSONResultSet)
        {
            return (SolrJSONResultSet) theResultSet;
        }

        return null;
    }
    public CollectionWithPagingInfo<TupleList> toCollectionWithPagingInfo(JSONArray docs, SearchSQLQuery searchQuery) throws JSONException
    {
        if(docs == null)
        {
            throw new RuntimeException("Solr response is required instead of docs:" + docs);
        }
        List<TupleList> entries = new ArrayList<TupleList>();
        for(int i = 0; i < docs.length() -1; i++)
        {
            List<TupleEntry> row = new ArrayList<TupleEntry>();
            JSONObject docObj = (JSONObject) docs.get(i);
            docObj.keys().forEachRemaining(action -> {
                try
                {
                    String value = docObj.get(action.toString()).toString();
                    row.add(new TupleEntry(action.toString(), value));
                } 
                catch (JSONException e)
                {
                    throw new RuntimeException("Unable to parse SQL response. " + e);
                }
            });
            entries.add(new TupleList(row));
        }
        Paging paging  = Paging.valueOf(0, 1000);
        return CollectionWithPagingInfo.asPaged(paging, entries);
    }
}
