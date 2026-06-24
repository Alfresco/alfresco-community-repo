/*
 * #%L
 * Alfresco Search Services E2E Test
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

package org.alfresco.rest.search;

import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the search functionality using an ancestor path. Using the category as an example, it is a node that is located in root/rootCategory/classifiable. We now provide the ability to search by path so that we can return all the child elements of our target path. A search on root/rootCategory/classifiable should return Regions, Languages as they are the child elements of the given path.
 *
 */

public class SearchAPATHTest extends AbstractSearchServicesE2ETest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        searchServicesDataPreparation();
        waitForContentIndexing(file4.getContent(), true);
    }

    /**
     * { "query": { "query": "name:*" }, "facetFields": { "facets": [ {"field": "APATH", "prefix": "0" } ] } } Expected result entries[], "pagination": { "maxItems": 100, "hasMoreItems": true, "totalItems": 914, "count": 100, "skipCount": 0 }, "context": { "facetsFields": [{ "buckets": [ { "count": 913, "label": "0/5c09534f-3ca2-4272-bc25-064a7c1762b4" }, { "count": 2, "label": "0/" } ], "label": "APATH" }], "consistency": {"lastTxId": 89} } }}
     *
     */
    @Test
    public void searchLevel0()
    {
        SearchRequest searchQuery = searchRequestWithAPATHFacet("name:*", "0");
        SearchResponse response = query(searchQuery);

        List<FacetFieldBucket> buckets = getBuckets(response);
        Assert.assertEquals(buckets.size(), 2);

        buckets.forEach(bucket -> bucket.assertThat().field("label").contains("0/"));
    }

    /**
     * Test to test that the facet buckets are returned correctly for sublevel 1/ Test to search for a searchString, that's unique to the test run and hence stable for any environment
     */
    @Test
    public void searchLevel0andIncludeSubLevel1()
    {
        SearchRequest searchQuery = searchRequestWithAPATHFacet("name:" + unique_searchString, "1/");
        SearchResponse response = query(searchQuery);

        List<FacetFieldBucket> buckets = getBuckets(response);
        Assert.assertEquals(buckets.size(), 1, "Incorrect bucket count");

        getFirstBucket(response).assertThat().field("label").contains("1/");
    }

    @Test
    public void searchLevel2()
    {
        String queryString = "name:cars";

        SearchRequest l1Request = searchRequestWithAPATHFacet(queryString, "1/");
        SearchResponse l1Response = query(l1Request);

        String l2Prefix = getFirstBucket(l1Response).getLabel().replaceFirst("^1/", "2/");

        SearchRequest l2Request = searchRequestWithAPATHFacet(queryString, l2Prefix);
        SearchResponse l2Response = query(l2Request);

        FacetFieldBucket bucket = getFirstBucket(l2Response);
        bucket.assertThat().field("label").contains(l2Prefix);

        String l3Prefix = bucket.getLabel().replaceFirst("^2/", "3/");

        SearchRequest l3Request = searchRequestWithAPATHFacet(queryString, l3Prefix);
        SearchResponse l3response = query(l3Request);

        List<FacetFieldBucket> buckets = getBuckets(l3response);
        Assert.assertEquals(buckets.size(), 1);

        getFirstBucket(l3response).assertThat().field("label").contains(l3Prefix);
    }

    /**
     * Creates a new {@link SearchRequest} for this test case.
     *
     * @param queryString
     *            the query string.
     * @param facetPrefix
     *            the facet prefix.
     * @return a new {@link SearchRequest} for this test case.
     */
    private SearchRequest searchRequestWithAPATHFacet(String queryString, String facetPrefix)
    {
        SearchRequest searchRequest = new SearchRequest();

        RestRequestQueryModel query = new RestRequestQueryModel();
        query.setQuery(queryString);
        searchRequest.setQuery(query);

        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        facetFields.setFacets(Collections.singletonList(new RestRequestFacetFieldModel("APATH", facetPrefix)));
        searchRequest.setFacetFields(facetFields);

        return searchRequest;
    }

    /**
     * Extracts the first bucket from the given response.
     *
     * @param response
     *            the results of a query execution.
     * @return the first bucket included in the search response.
     */
    private FacetFieldBucket getFirstBucket(SearchResponse response)
    {
        return getBuckets(response).iterator().next();
    }

    /**
     * Extracts the buckets from the given response. The method also makes sure the buckets list is not empty in the input response.
     *
     * @param response
     *            the results of a query execution.
     * @return the getBuckets included in the search response.
     */
    private List<FacetFieldBucket> getBuckets(SearchResponse response)
    {
        List<RestResultBucketsModel> facetFields = response.getContext().getFacetsFields();
        Assert.assertNotNull(facetFields);
        Assert.assertFalse(facetFields.isEmpty());

        List<FacetFieldBucket> buckets = facetFields.iterator().next().getBuckets();
        Assert.assertNotNull(buckets);
        Assert.assertFalse(buckets.isEmpty());

        return buckets;
    }
}
