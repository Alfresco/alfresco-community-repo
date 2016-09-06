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

import org.alfresco.repo.search.impl.lucene.PagingLuceneResultSet;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.search.model.SearchEntry;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.SearchContext;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.util.ParameterCheck;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps from a Solr ResultSet to a json public api representation.
 *
 * @author Gethin James
 */
public class ResultMapper
{

    private Nodes nodes;

    public ResultMapper(Nodes nodes)
    {
        this.nodes = nodes;
        ParameterCheck.mandatory("nodes", this.nodes);
    }

    /**
     *
     * @param params
     * @param results
     * @return
     */
    public CollectionWithPagingInfo<Node> toCollectionWithPagingInfo(Params params, ResultSet results)
    {
        SearchQuery searchQuery = (SearchQuery) params.getPassedIn();
        SearchContext context = null;
        Long totalItems = results.getNumberFound();
        List<Node> noderesults = new ArrayList();

        results.forEach(row ->
        {
            Node aNode = nodes.getFolderOrDocument(row.getNodeRef(), null, null, params.getInclude(), null);
            float f = row.getScore();
            aNode.setSearch(new SearchEntry(f));
            noderesults.add(aNode);
        }
        );

        Integer total = Integer.valueOf(totalItems.intValue());
        int skip = searchQuery.getPaging()==null?0:searchQuery.getPaging().getSkipCount();

        SolrJSONResultSet jsonResultSet = findJsonResults(results);

        if (jsonResultSet != null)
        {
            context = new SearchContext(jsonResultSet.getLastIndexedTxId());
        }

        return CollectionWithPagingInfo.asPaged(searchQuery.getPaging(), noderesults, noderesults.size() + skip < total, total, null, context);
    }

    /**
     * Gets SolrJSONResultSet class if there is one.
     * @param results
     * @return
     */
    protected SolrJSONResultSet findJsonResults(ResultSet results)
    {
        //This may get more complicated if the results are wrapped in another ResultSet class
        if (results instanceof SolrJSONResultSet)
        {
            return (SolrJSONResultSet) results;
        }
/**
        if (results instanceof PagingLuceneResultSet)
        {
            return findJsonResults(((PagingLuceneResultSet) results).getWrapped());
        }
**/
        return null;
    }
}
