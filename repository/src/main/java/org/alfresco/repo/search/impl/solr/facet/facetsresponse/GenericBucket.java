/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Generic Bucket response covering range, interval, pivot etc.
 */
public class GenericBucket
{
    private final String label;
    private final String filterQuery;
    private final Object display;
    private final Set<Metric> metrics;
    private Map<String, String> bucketInfo; //Additional information relating to facets such as to and from.
    private final List<GenericFacetResponse> facets;

    /**
     * 
     * @param label
     * @param filterQuery
     * @param display
     * @param metrics
     * @param facets
     */
    public GenericBucket(String label, String filterQuery, Object display, Set<Metric> metrics, List<GenericFacetResponse> facets)
    {
        this.label = label;
        this.filterQuery = filterQuery;
        this.display = display;
        this.metrics = metrics;
        this.facets = facets;
        
    }
    public GenericBucket(String label, String filterQuery, Object display, Set<Metric> metrics, List<GenericFacetResponse> facets, Map<String, String> bucketInfo)
    {
        this.label = label;
        this.filterQuery = filterQuery;
        this.display = display;
        this.metrics = metrics;
        this.facets = facets;
        this.bucketInfo = bucketInfo;
    }

    public String getFilterQuery()
    {
        return filterQuery;
    }

    public Object getDisplay()
    {
        return display;
    }

    public String getLabel()
    {
        return label;
    }

    public Set<Metric> getMetrics()
    {
        return metrics;
    }
    
    public List<GenericFacetResponse> getFacets()
    {
        return facets;
    }
    public Map<String, String> getBucketInfo()
    {
        return bucketInfo;
    }
    
    public void setBucketInfo(Map<String, String> bucketInfo)
    {
        this.bucketInfo = bucketInfo;
    }
}
