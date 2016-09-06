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

import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.api.search.context.SpellCheckContext;
import org.alfresco.rest.api.search.model.SearchEntry;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.api.search.context.SearchContext;
import org.alfresco.rest.api.search.context.FacetQueryContext;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private Nodes nodes;
    private static Log logger = LogFactory.getLog(ResultMapper.class);

    public ResultMapper(Nodes nodes)
    {
        this.nodes = nodes;
        ParameterCheck.mandatory("nodes", this.nodes);
    }

    /**
     * Turns the results into a CollectionWithPagingInfo
     * @param params
     * @param results
     * @return CollectionWithPagingInfo<Node>
     */
    public CollectionWithPagingInfo<Node> toCollectionWithPagingInfo(SearchQuery searchQuery, ResultSet results)
    {
        SearchContext context = null;
        Integer total = null;
        List<Node> noderesults = new ArrayList();
        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        results.forEach(row ->
        {
            Node aNode = nodes.getFolderOrDocument(row.getNodeRef(), null, null, searchQuery.getInclude(), mapUserInfo);
            if (aNode != null)
            {
                float f = row.getScore();
                aNode.setSearch(new SearchEntry(f));
                noderesults.add(aNode);
            }
            else
            {
                logger.debug("Unknown noderef returned from search results "+row.getNodeRef());
            }
        });

        SolrJSONResultSet solrResultSet = findSolrResultSet(results);

        if (solrResultSet != null)
        {
            //We used Solr for this query
            context = toSearchContext(solrResultSet);
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

        return CollectionWithPagingInfo.asPaged(searchQuery.getPaging(), noderesults, results.hasMore(), total, null, context);
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
    public SearchContext toSearchContext(SolrJSONResultSet solrResultSet)
    {
        SearchContext context = null;
        Map<String, Integer> facetQueries = solrResultSet.getFacetQueries();
        List<FacetQueryContext> facetResults = null;
        SpellCheckContext spellCheckContext = null;

        if(facetQueries!= null && !facetQueries.isEmpty())
        {
            facetResults = new ArrayList<>(facetQueries.size());
            for (Entry<String, Integer> fq:facetQueries.entrySet())
            {
                facetResults.add(new FacetQueryContext(fq.getKey(), fq.getValue()));
            }
        }

        SpellCheckResult spell = solrResultSet.getSpellCheckResult();
        if (spell != null && spell.getResultName() != null && !spell.getResults().isEmpty())
        {
            spellCheckContext = new SpellCheckContext(spell.getResultName(),spell.getResults());
        }
        context = new SearchContext(solrResultSet.getLastIndexedTxId(), facetResults, spellCheckContext);
        return isNullContext(context)?null:context;
    }

    /**
     * Is the context null?
     * @param context
     * @return true if its null
     */
    protected boolean isNullContext(SearchContext context)
    {
        return (context.getFacetQueries() == null && context.getConsistency() == null && context.getSpellCheck() == null);
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
