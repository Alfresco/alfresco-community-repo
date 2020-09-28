/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @see PathMapper 
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PathMapperTest extends TestCase
{
    private PathMapper mapper;
    
    @Override
    protected void setUp() throws Exception
    {
        mapper = new PathMapper();
        mapper.addPathMap("/a/b/c", "/1/2/3");
        mapper.addPathMap("/a/b/c", "/one/two/three");
        mapper.addPathMap("/a/c/c", "/1/3/3");
        mapper.addPathMap("/a/c/c", "/one/three/three");
        mapper.addPathMap("/A/B/C", "/1/2/3");
        mapper.addPathMap("/A/B/C", "/ONE/TWO/THREE");
        mapper.addPathMap("/A/C/C", "/1/3/3");
        mapper.addPathMap("/A/C/C", "/ONE/THREE/THREE");
    }
    
    public void testConvertValueMap()
    {
        Map<String, Integer> inputMap = new HashMap<String, Integer>(5);
        inputMap.put("/a/a/a/111", 111);
        inputMap.put("/a/b/c/123", 123);
        inputMap.put("/a/b/b/122", 122);
        inputMap.put("/a/c/c/133", 133);
        inputMap.put("/A/A/A/111", 111);
        inputMap.put("/A/B/C/123", 123);
        inputMap.put("/A/B/B/122", 122);
        inputMap.put("/A/C/C/133", 133);
        
        Map<String, Integer> expectedOutputMap = new HashMap<String, Integer>(5);
        expectedOutputMap.put("/1/2/3/123", 123);
        expectedOutputMap.put("/one/two/three/123", 123);
        expectedOutputMap.put("/1/3/3/133", 133);
        expectedOutputMap.put("/one/three/three/133", 133);
        expectedOutputMap.put("/1/2/3/123", 123);
        expectedOutputMap.put("/ONE/TWO/THREE/123", 123);
        expectedOutputMap.put("/1/3/3/133", 133);
        expectedOutputMap.put("/ONE/THREE/THREE/133", 133);
        
        Map<String, Integer> outputMap = mapper.convertMap(inputMap);
        
        String diff = EqualsHelper.getMapDifferenceReport(outputMap, expectedOutputMap);
        if (diff != null)
        {
            fail(diff);
        }
    }
    
    public void testPathMatchesExact()
    {
        Set<String> mappedPaths = mapper.getMappedPaths("/a/b/c");
        assertEquals("Exact matches expected", 2, mappedPaths.size());
        mappedPaths = mapper.getMappedPaths("/a");
        assertEquals("Exact match NOT expected", 0, mappedPaths.size());
    }
    
    public void testPathMatchesPartial()
    {
        Set<String> mappedPaths = mapper.getMappedPathsWithPartialMatch("/a");
        assertEquals("Partial matches expected", 4, mappedPaths.size());
    }
}
