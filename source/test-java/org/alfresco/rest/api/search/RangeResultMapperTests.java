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
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.service.cmr.search.RangeParameters;
import org.junit.Test;

/**
 * Tests the RangeResultMapper class
 *
 * @author Michael Suzuki
 */
public class RangeResultMapperTests
{
    static Map<String,String> facet = new HashMap<String, String>();
    {
        facet.put("start", "0");
        facet.put("end", "10");
    }

    @Test
    public void testBuildGenericBucketFromRange() throws Exception
    {
        //=============== start bucket
        GenericBucket response = queryWithInclude(null, "head");
        assertEquals("test:[0 TO 10>", response.getFilterQuery());
        
        response = queryWithInclude("outer", "head");
        assertEquals("test:[0 TO 10]", response.getFilterQuery());
        
        response = queryWithInclude("lower", "head");
        assertEquals("test:[0 TO 10<", response.getFilterQuery());
        
        response = queryWithInclude("edge", "head");
        assertEquals("test:[0 TO 10<", response.getFilterQuery());
        
        response = queryWithInclude("upper", "head");
        assertEquals("test:]0 TO 10>", response.getFilterQuery());
        
        //=============== Non start end bucket
        response = queryWithInclude("lower", "body");
        assertEquals("test:[0 TO 10<", response.getFilterQuery());
        response = queryWithInclude("upper", "body");
        assertEquals("test:]0 TO 10>", response.getFilterQuery());
        //=============== End bucket
        response = queryWithInclude("lower", "tail");
        assertEquals("test:[0 TO 10>", response.getFilterQuery());

        response = queryWithInclude("edge", "tail");
        assertEquals("test:]0 TO 10]", response.getFilterQuery());

        response = queryWithInclude("upper", "tail");
        assertEquals("test:]0 TO 10]", response.getFilterQuery());
    }
    
    private GenericBucket queryWithInclude(String param, String bucketPosition)
    {
   
        List<RangeParameters> ranges = new ArrayList<RangeParameters>();
        if(param != null && !param.isEmpty())
        {
            List<String> include = new ArrayList<String>();
            include.add(param);
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
}
