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
package org.alfresco.rest.api.search;

import static junit.framework.TestCase.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericBucket;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.RangeResultMapper;
import org.alfresco.service.cmr.search.RangeParameters;
import org.junit.Test;

/**
 * Tests the RangeResultMapper class
 *
 * @author Michael Suzuki
 */
public class RangeResultMapperTests
{

    @Test
    public void testBuildGenericBucketFromRange() throws Exception
    {
      //=============== Default
        GenericBucket response = queryWithoutInclude("head");
        assertEquals("test:[\"0\" TO \"10\">", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("false", response.getBucketInfo().get("endInclusive"));
        response = queryWithoutInclude("body");
        assertEquals("test:[\"0\" TO \"10\">", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("false", response.getBucketInfo().get("endInclusive"));
        response = queryWithoutInclude("tail");
        assertEquals("test:[\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
        //=============== start bucket
        response = queryWithInclude("head", null);
        assertEquals("test:[\"0\" TO \"10\">", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("false", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithInclude("head","outer");
        assertEquals("test:<\"0\" TO \"10\">", response.getFilterQuery());
        assertEquals("false", response.getBucketInfo().get("startInclusive"));
        assertEquals("false", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithInclude("head","lower");
        assertEquals("test:[\"0\" TO \"10\">", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("false", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithInclude("head","upper","lower");
        assertEquals("test:[\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithInclude("head","edge","upper");
        assertEquals("test:[\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithInclude("head","upper");
        assertEquals("test:<\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("false", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
        
        //=============== Non start end bucket
        response = queryWithInclude("body","lower");
        assertEquals("test:[\"0\" TO \"10\">", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("false", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithInclude("body","upper");
        assertEquals("test:<\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("false", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
        //=============== End bucket
        response = queryWithInclude("tail","lower");
        assertEquals("test:[\"0\" TO \"10\">", response.getFilterQuery());
        assertEquals("true", response.getBucketInfo().get("startInclusive"));
        assertEquals("false", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithInclude("tail","edge");
        assertEquals("test:<\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("false", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithInclude("tail","upper");
        assertEquals("test:<\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("false", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
        
        //Before
        response = queryWithInclude("head","before");
        assertEquals("test:<\"0\" TO \"10\">", response.getFilterQuery());
        assertEquals("false", response.getBucketInfo().get("startInclusive"));
        assertEquals("false", response.getBucketInfo().get("endInclusive"));
        
        response = queryWithIncludeAndOther("head","outer","before");
        assertEquals("test:<\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("false", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
        
        //After
        response = queryWithIncludeAndOther("head","outer","after");
        assertEquals("test:<\"0\" TO \"10\"]", response.getFilterQuery());
        assertEquals("false", response.getBucketInfo().get("startInclusive"));
        assertEquals("true", response.getBucketInfo().get("endInclusive"));
    }
    
    private GenericBucket queryWithInclude(String bucketPosition, String ...includeParam)
    {
   
        List<RangeParameters> ranges = new ArrayList<RangeParameters>();
        if(includeParam != null && includeParam.length >= 1)
        {
            List<String> include = new ArrayList<String>();
            for(int i = 0; i < includeParam.length; i++)
            {
                include.add(includeParam[i]);
            }
            ranges.add(new RangeParameters("test", "0", "10", "1", true, null, include, null, null));
        }
        Map<String,String> facet = new HashMap<String,String>();
        facet.put("bucketPosition", bucketPosition);
        facet.put(GenericFacetResponse.LABEL, "test");
        facet.put(GenericFacetResponse.COUNT, "11");
        facet.put(GenericFacetResponse.START, "0");
        facet.put(GenericFacetResponse.END, "10");
        return RangeResultMapper.buildGenericBucketFromRange("test", facet, ranges);
    }
    private Map<String,String> buildFaet(String bucketPosition)
    {
        Map<String,String> facet = new HashMap<String,String>();
        facet.put("bucketPosition", bucketPosition);
        facet.put(GenericFacetResponse.LABEL, "test");
        facet.put(GenericFacetResponse.COUNT, "11");
        facet.put(GenericFacetResponse.START, "0");
        facet.put(GenericFacetResponse.END, "10");
        return facet;
    }
    private GenericBucket queryWithIncludeAndOther(String bucketPosition, String includeParam, String otherParam)
    {
        List<RangeParameters> ranges = new ArrayList<RangeParameters>();
        List<String> include = new ArrayList<String>();
        include.add(includeParam);
        List<String> other = new ArrayList<String>();
        other.add(otherParam);
        ranges.add(new RangeParameters("test", "0", "10", "1", true, other, include, null, null));
        return RangeResultMapper.buildGenericBucketFromRange("test", buildFaet(bucketPosition), ranges);
    }
    private GenericBucket queryWithoutInclude(String bucketPosition)
    {
        List<RangeParameters> ranges = new ArrayList<RangeParameters>();
        ranges.add(new RangeParameters("test", "0", "10", "1", true, null, null, null, null));
        return RangeResultMapper.buildGenericBucketFromRange("test", buildFaet(bucketPosition), ranges);
    }
}
