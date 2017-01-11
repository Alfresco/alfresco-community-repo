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
import static org.alfresco.rest.api.search.impl.StoreMapper.LIVE_NODES;
import static org.alfresco.rest.api.search.impl.StoreMapper.VERSIONS;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
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
import org.alfresco.rest.api.search.context.SpellCheckContext;
import org.alfresco.rest.api.search.model.HighlightEntry;
import org.alfresco.rest.api.search.model.SearchEntry;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
     * @param results
     * @return CollectionWithPagingInfo<Node>
     */
    public CollectionWithPagingInfo<Node> toCollectionWithPagingInfo(Params params, ResultSet results)
    {
        SearchContext context = null;
        Integer total = null;
        List<Node> noderesults = new ArrayList();
        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);
        Map<NodeRef, List<Pair<String, List<String>>>> hightLighting = results.getHighlighting();
        int notFound = 0;

        for (ResultSetRow row:results)
        {
            Node aNode = getNode(row, params, mapUserInfo);

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
            context = toSearchContext(solrResultSet, notFound);
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
     * @param aRow
     * @param params
     * @param mapUserInfo
     * @return Node
     */
    public Node getNode(ResultSetRow aRow, Params params, Map<String, UserInfo> mapUserInfo)
    {
        String nodeStore = storeMapper.getStore(aRow.getNodeRef());
        Node aNode = null;
        switch (nodeStore)
        {
            case LIVE_NODES:
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
     * @return SearchContext
     */
    public SearchContext toSearchContext(SolrJSONResultSet solrResultSet, int notFound)
    {
        SearchContext context = null;
        Map<String, Integer> facetQueries = solrResultSet.getFacetQueries();
        List<FacetQueryContext> facetResults = null;
        SpellCheckContext spellCheckContext = null;
        List<FacetFieldContext> ffcs = null;

        //Facet queries
        if(facetQueries!= null && !facetQueries.isEmpty())
        {
            facetResults = new ArrayList<>(facetQueries.size());
            for (Entry<String, Integer> fq:facetQueries.entrySet())
            {
                facetResults.add(new FacetQueryContext(fq.getKey(), fq.getValue()));
            }
        }

        //Field Facets
        Map<String, List<Pair<String, Integer>>> facetFields = solrResultSet.getFieldFacets();
        if (facetFields != null && !facetFields.isEmpty())
        {
            ffcs = new ArrayList<>(facetFields.size());
            for (Entry<String, List<Pair<String, Integer>>> facet:facetFields.entrySet())
            {
                if (facet.getValue() != null && !facet.getValue().isEmpty())
                {
                    List<Bucket> buckets = new ArrayList<>(facet.getValue().size());
                    for (Pair<String, Integer> buck:facet.getValue())
                    {
                        Object display = propertyLookup.lookup(facet.getKey(), buck.getFirst());
                        buckets.add(new Bucket(buck.getFirst(), buck.getSecond(), display));
                    }
                    ffcs.add(new FacetFieldContext(facet.getKey(), buckets));
                }
            }
        }

        //Spelling
        SpellCheckResult spell = solrResultSet.getSpellCheckResult();
        if (spell != null && spell.getResultName() != null && !spell.getResults().isEmpty())
        {
            spellCheckContext = new SpellCheckContext(spell.getResultName(),spell.getResults());
        }

        //Put it all together
        context = new SearchContext(solrResultSet.getLastIndexedTxId(), facetResults, ffcs, spellCheckContext);
        return isNullContext(context)?null:context;
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
                    && context.getFacetsFields() == null);
    }

    /**
     * Gets SolrJSONResultSet class if there is one.
     * @param results
     * @return
     */
    protected SolrJSONResultSet findSolrResultSet(ResultSet results)
    {
        //This may get more complicated if the results are wrapped in another ResultSet class
        if (results instanceof SolrJSONResultSet)
        {
            return (SolrJSONResultSet) results;
        }
/**
        if (results instanceof PagingLuceneResultSet)
        {
            return findSolrResultSet(((PagingLuceneResultSet) results).getWrapped());
        }
**/
        return null;
    }
}
