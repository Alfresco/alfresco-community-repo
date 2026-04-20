/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.node.sizedetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.node.sizedetails.NodeSizeDetailsServiceImpl.NodeSizeDetails;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;

/**
 * Unit tests for {@link NodeSizeDetailsServiceImpl#calculateTotalSizeFromFacet}.
 */
public class NodeSizeDetailsServiceImplTest
{
    private static final String FIELD_FACET = "content.size";
    private static final int DEFAULT_ITEMS = 3;

    private NodeSizeDetailsServiceImpl nodeSizeService;
    private SearchService searchService;

    @Before
    public void setUp()
    {
        nodeSizeService = new NodeSizeDetailsServiceImpl();
        searchService = mock(SearchService.class);
        nodeSizeService.setSearchService(searchService);
        nodeSizeService.setDefaultItems(DEFAULT_ITEMS);
    }

    private void mockResults(List<Pair<String, Integer>> facets)
    {
        ResultSet firstResult = mock(ResultSet.class);
        when(firstResult.getNumberFound()).thenReturn((long) facets.size());

        ResultSet facetResult = mock(ResultSet.class);
        when(facetResult.getFieldFacet(FIELD_FACET)).thenReturn(facets);
        when(facetResult.getNumberFound()).thenReturn((long) facets.size());

        when(searchService.query(any())).thenReturn(firstResult, facetResult);
    }

    @Test
    public void testCalculateTotalSizeFromFacet_subsetBelowDefaultItems()
    {
        NodeRef nodeRef = new NodeRef("workspace", "SpacesStore", "test-node-1");
        List<Pair<String, Integer>> facets = Arrays.asList(
                new Pair<>("100", 1),
                new Pair<>("200", 2));

        mockResults(facets);
        NodeSizeDetails result = nodeSizeService.calculateTotalSizeFromFacet(nodeRef, "job-1");

        assertNotNull(result);
        assertEquals(500L, (long) result.getSizeInBytes());
        assertEquals(NodeSizeDetails.STATUS.COMPLETED, result.getStatus());
        assertEquals("job-1", result.getJobId());
    }

    @Test
    public void testCalculateTotalSizeFromFacet_equalToDefaultItems()
    {
        NodeRef nodeRef = new NodeRef("workspace", "SpacesStore", "test-node-2");
        List<Pair<String, Integer>> facets = Arrays.asList(
                new Pair<>("100", 1),
                new Pair<>("200", 2),
                new Pair<>("300", 1));

        mockResults(facets);
        NodeSizeDetails result = nodeSizeService.calculateTotalSizeFromFacet(nodeRef, "job-2");

        assertNotNull(result);
        assertEquals(800L, (long) result.getSizeInBytes());
        assertEquals(NodeSizeDetails.STATUS.COMPLETED, result.getStatus());
        assertEquals("job-2", result.getJobId());
    }

    @Test
    public void testCalculateTotalSizeFromFacet_greaterThanDefaultItems()
    {
        NodeRef nodeRef = new NodeRef("workspace", "SpacesStore", "test-node-3");
        List<Pair<String, Integer>> facets = Arrays.asList(
                new Pair<>("100", 1),
                new Pair<>("200", 2),
                new Pair<>("300", 1),
                new Pair<>("400", 3),
                new Pair<>("500", 2));

        mockResults(facets);
        NodeSizeDetails result = nodeSizeService.calculateTotalSizeFromFacet(nodeRef, "job-3");

        assertNotNull(result);
        assertEquals(3000L, (long) result.getSizeInBytes());
        assertEquals(NodeSizeDetails.STATUS.COMPLETED, result.getStatus());
        assertEquals("job-3", result.getJobId());
    }
}
