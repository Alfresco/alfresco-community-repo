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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericBucket;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse.FACET_TYPE;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.Metric;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.Metric.METRIC_TYPE;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.SimpleMetric;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.service.cmr.search.RangeParameters;

/**Helper to map range results.
 *
 * @author Michael Suzuki
 */
public class RangeResultMapper
{
    /**
     * Transforms the facet range response into generic facet response.
     * @param facetFields
     * @param searchQuery
     * @return GenericFacetResponse
     */
    public static List<GenericFacetResponse> getGenericFacetsForRanges( Map<String,List<Map<String,String>>> facetFields, SearchQuery searchQuery)
    {
        List<GenericFacetResponse> ffcs = new ArrayList<>(facetFields.size());
        if (facetFields != null && !facetFields.isEmpty() && searchQuery.getQuery() != null)
        {
            for (Entry<String, List<Map<String, String>>> facet : facetFields.entrySet())
            {
                List<GenericBucket> buckets = new ArrayList<>();
                facet.getValue().forEach(action -> buckets.add(buildGenericBucketFromRange(facet.getKey(),
                        (Map<String, String>) action,
                        searchQuery.getFacetRanges())));
                ffcs.add(new GenericFacetResponse(FACET_TYPE.range, facet.getKey(), buckets));
            }
        }
        return ffcs;
    }
    private static boolean isRangeStartInclusive(List<String> values)
    {
        if(values != null && !values.isEmpty())
        {
            for(String startInc : values)
            switch (startInc)
            {
            case "upper":
                return  false;
            case "outer":
                return  false;
            default:
                break;
            }
        }
        return true;
    }
    private static boolean isRangeEndInclusive(List<String> endIncs)
    {
        if(endIncs != null && !endIncs.isEmpty())
        {
            for(String endInc : endIncs)
            {
                switch (endInc)
                {
                case "upper":
                    return  true;
                case "edge":
                    return  true;
                case "outer":
                    return  true;
                case "all":
                    return  true;
                default:
                    break;
                }
            }
        }
        return false;
    }
    /**
     * Builds the generic facet response out of range results.
     * @param facetField
     * @param facet
     * @return
     */
    private static GenericBucket buildGenericBucketFromRange(String facetField, Map<String,String> facet, List<RangeParameters> ranges)
    {
        String start = facet.get(GenericFacetResponse.START);
        String end = facet.get(GenericFacetResponse.END);
        boolean startInclusive = true;
        boolean endInclusive = false;
        
        for(RangeParameters range : ranges)
        {
            if(range.getField().equalsIgnoreCase(facetField))
            {
                List<String> includes = range.getInclude();
                if(includes != null && !includes.isEmpty())
                {
                    startInclusive = isRangeStartInclusive(includes); 
                    endInclusive = isRangeEndInclusive(includes);
                }
            }
        }
        
        facet.put(GenericFacetResponse.START_INC.toString(), Boolean.toString(startInclusive));
        facet.put(GenericFacetResponse.END_INC.toString(), Boolean.toString(endInclusive));
  
        facet.remove(GenericFacetResponse.LABEL);
        StringBuilder filterQ = new StringBuilder();
        filterQ.append(facetField).append(":")
            .append(startInclusive ? "[" :"<")
            .append(start).append(" TO ")
            .append(end)
            .append(endInclusive ? "]" :">");
        
        Set<Metric> metrics = new HashSet<Metric>(
                Arrays.asList(new SimpleMetric(
                        METRIC_TYPE.count,facet.get(
                                GenericFacetResponse.COUNT))));
        facet.remove("count");
        
        StringBuilder label = new StringBuilder();
        label.append(startInclusive ? "[" :"(")
             .append(start)
             .append(" - ")
             .append(end)
             .append(endInclusive ? "]" :")");
        
        return new GenericBucket(label.toString(),
                                 filterQ.toString(),
                                 null, 
                                 metrics,
                                 null,
                                 facet);
        
    }
}
