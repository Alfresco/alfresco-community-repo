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

package org.alfresco.repo.search.impl.solr.facet.facetsresponse;

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
    public static List<GenericFacetResponse> getGenericFacetsForRanges(Map<String,List<Map<String,String>>> facetFields, List<RangeParameters> ranges)
    {
        List<GenericFacetResponse> ffcs = new ArrayList<>(facetFields.size());
        if (facetFields != null && !facetFields.isEmpty() && ranges != null)
        {
            for (Entry<String, List<Map<String, String>>> facet : facetFields.entrySet())
            {
                List<GenericBucket> buckets = new ArrayList<>();
                facet.getValue().forEach(action -> buckets.add(buildGenericBucketFromRange(facet.getKey(),
                        (Map<String, String>) action, ranges)));
                ffcs.add(new GenericFacetResponse(FACET_TYPE.range, facet.getKey(), buckets));
            }
        }
        return ffcs;
    }
    
    /**
     * Builds the generic facet response out of range results.
     * @param facetField
     * @param facet
     * @return
     */
    public static GenericBucket buildGenericBucketFromRange(String facetField, Map<String,String> facet, List<RangeParameters> ranges)
    {
        String start = facet.get(GenericFacetResponse.START);
        String end = facet.get(GenericFacetResponse.END);
        boolean startInclusive = true;
        boolean endInclusive = false;
        String startFilterQuery = "[";
        String endFilterQuery = ">";
        StringBuilder filterQ = new StringBuilder();
        //Check if other 
        //We take the position of the bucket into consideration.
        switch (facet.get("bucketPosition"))
        {
            case "head":
                for(RangeParameters range : ranges)
                {
                    if(range.getField().equalsIgnoreCase(facetField))
                    {
                        startFilterQuery = range.getRangeFirstBucketStartInclusive();
                        endFilterQuery = range.getRangeFirstBucketEndInclusive();
                        startInclusive = checkInclusive(startFilterQuery); 
                        endInclusive = checkInclusive(endFilterQuery);
                    }
                }
                break;
            case "tail":
                for(RangeParameters range : ranges)
                {
                    if(range.getField().equalsIgnoreCase(facetField))
                    {
                        startFilterQuery = range.getRangeBucketStartInclusive();
                        endFilterQuery = range.getRangeLastBucketEndInclusive();
                        startInclusive = checkInclusive(startFilterQuery); 
                        endInclusive = checkInclusive(endFilterQuery);
                    }
                }
                break;
            default:
                for(RangeParameters range : ranges)
                {
                    if(range.getField().equalsIgnoreCase(facetField))
                    {
                        List<String> includes = range.getInclude();
                        if(includes != null && !includes.isEmpty())
                        {
                            startFilterQuery = range.getRangeBucketStartInclusive();
                            endFilterQuery = range.getRangeBucketEndInclusive();
                            startInclusive = checkInclusive(startFilterQuery); 
                            endInclusive = checkInclusive(endFilterQuery);
                        }
                    }
                }
                break;
        }
        
        facet.put(GenericFacetResponse.START_INC.toString(), Boolean.toString(startInclusive));
        facet.put(GenericFacetResponse.END_INC.toString(), Boolean.toString(endInclusive));
  
        facet.remove(GenericFacetResponse.LABEL);
        filterQ.append(facetField).append(":")
            .append(startFilterQuery)
            .append("\"").append(start).append("\"")
            .append(" TO ")
            .append("\"").append(end).append("\"")
            .append(endFilterQuery);
        
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
        facet.remove("bucketPosition");
        
        return new GenericBucket(label.toString(),
                                 filterQ.toString(),
                                 null, 
                                 metrics,
                                 null,
                                 facet);
        
    }
    private static boolean checkInclusive(String input)
    {
        return input.equalsIgnoreCase("[") || input.equalsIgnoreCase("]")? true:false; 
    }
}
