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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestRequestRangesModel;

/**
 * Faceted Range Search Query for numeric range { "query": { "query": "name:A*" }, "range": { "field": "content.size", "start": "0", "end": "400", "gap": "100" } } Date range query: { "query": { "query": "name:A*" }, "range": { "field": "created", "start": "2015-09-29T10:45:15.729Z", "end": "2016-09-29T10:45:15.729Z", "gap": "+100DAY" } }
 */
public class FacetRangeSearchTest extends AbstractSearchServicesE2ETest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        searchServicesDataPreparation();
        waitForContentIndexing(file4.getContent(), true);
    }

    /** Check the error messages mention the mandatory fields when they are omitted. */
    @Test
    public void checkingFacetsMandatoryErrorMessages()
    {
        SearchRequest query = createQuery("cars");

        // Omit the field.
        query.setRanges(List.of(createRangesModel(null, "0", "400", "20")));
        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "field"));

        // Omit the start.
        query.setRanges(List.of(createRangesModel("content.size", null, "400", "20")));
        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "start"));

        // Omit the end.
        query.setRanges(List.of(createRangesModel("content.size", "0", null, "20")));
        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "end"));

        // Omit the gap.
        query.setRanges(List.of(createRangesModel("content.size", "0", "400", null)));
        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "gap"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void searchWithRange()
    {
        SearchRequest query = createQuery("* AND SITE:'" + testSite.getId() + "'");

        RestRequestRangesModel facetRangeModel = createRangesModel("content.size", "0", "200", "20");
        List<RestRequestRangesModel> ranges = List.of(facetRangeModel);
        query.setRanges(ranges);
        SearchResponse response = query(query);
        response.assertThat().entriesListIsNotEmpty();
        response.getContext().assertThat().field("facets").isNotEmpty();
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().getFirst();

        RestGenericBucketModel bucket = facetResponseModel.getBuckets().getFirst();
        bucket.assertThat().field("label").is("[20 - 40)");
        bucket.assertThat().field("filterQuery").is("content.size:[\"20\" TO \"40\">");
        Map<String, String> metric = (Map<String, String>) bucket.getMetrics().getFirst().getValue();
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 2, "Unexpected count for first bucket.");
        Map<String, String> info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "20");
        assertEquals(info.get("end"), "40");
        assertNull(info.get("count"));
        assertEquals(info.get("startInclusive"), "true");
        assertEquals(info.get("endInclusive"), "false");

        bucket = facetResponseModel.getBuckets().get(1);
        bucket.assertThat().field("label").is("[40 - 120)");
        bucket.assertThat().field("filterQuery").is("content.size:[\"40\" TO \"120\">");
        metric = (Map<String, String>) bucket.getMetrics().getFirst().getValue();
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 1, "Unexpected count for second bucket.");
        info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "40");
        assertEquals(info.get("end"), "120");
        assertEquals(info.get("startInclusive"), "true");
        assertEquals(info.get("endInclusive"), "false");

        bucket = facetResponseModel.getBuckets().get(2);
        bucket.assertThat().field("label").is("[120 - 200]");
        bucket.assertThat().field("filterQuery").is("content.size:[\"120\" TO \"200\"]");
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 1, "Unexpected count for third bucket.");
        info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "120");
        assertEquals(info.get("end"), "200");
        assertEquals(info.get("startInclusive"), "true");
        assertEquals(info.get("endInclusive"), "true");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void searchWithRangeHardend()
    {
        SearchRequest query = createQuery("* AND SITE:'" + testSite.getId() + "'");

        RestRequestRangesModel facetRangeModel = createRangesModel("content.size", "0", "200", "20");
        facetRangeModel.setHardend(true);
        List<RestRequestRangesModel> ranges = List.of(facetRangeModel);
        query.setRanges(ranges);
        SearchResponse response = query(query);
        response.assertThat().entriesListIsNotEmpty();
        response.getContext().assertThat().field("facets").isNotEmpty();
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().getFirst();

        RestGenericBucketModel bucket = facetResponseModel.getBuckets().getFirst();
        bucket.assertThat().field("label").is("[20 - 40)");
        bucket.assertThat().field("filterQuery").is("content.size:[\"20\" TO \"40\">");
        Map<String, String> metric = (Map<String, String>) bucket.getMetrics().getFirst().getValue();
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 2, "Unexpected count for first bucket.");
        Map<String, String> info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "20");
        assertEquals(info.get("end"), "40");
        assertEquals(info.get("startInclusive"), "true");
        assertEquals(info.get("endInclusive"), "false");
        assertNull(info.get("count"));

        bucket = facetResponseModel.getBuckets().get(1);
        bucket.assertThat().field("label").is("[40 - 120)");
        bucket.assertThat().field("filterQuery").is("content.size:[\"40\" TO \"120\">");
        info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "40");
        assertEquals(info.get("end"), "120");
        metric = (Map<String, String>) bucket.getMetrics().getFirst().getValue();
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 1, "Unexpected count for second bucket.");
        assertNull(info.get("count"));
        assertEquals(info.get("startInclusive"), "true");
        assertEquals(info.get("endInclusive"), "false");

        bucket = facetResponseModel.getBuckets().get(2);
        bucket.assertThat().field("label").is("[120 - 200]");
        bucket.assertThat().field("filterQuery").is("content.size:[\"120\" TO \"200\"]");
        metric = (Map<String, String>) bucket.getMetrics().getFirst().getValue();
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 1, "Unexpected count for third bucket.");
        info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "120");
        assertEquals(info.get("end"), "200");
        assertNull(info.get("count"));
        assertEquals(info.get("startInclusive"), "true");
        assertEquals(info.get("endInclusive"), "true");
    }

    @Test
    public void searchDateAndSizeRanges()
    {
        SearchRequest query = createQuery("* AND SITE:'" + testSite.getId() + "'");
        RestRequestRangesModel facetRangeModel = createRangesModel("created", "2015-09-29T10:45:15.729Z", "2016-09-29T10:45:15.729Z", "+280DAY");
        RestRequestRangesModel facetCountRangeModel = createRangesModel("content.size", "0", "500", "200");
        List<RestRequestRangesModel> ranges = List.of(facetRangeModel, facetCountRangeModel);
        query.setRanges(ranges);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void searchWithRangeAndIncludeUpperBound()
    {
        SearchRequest query = createQuery("* AND SITE:'" + testSite.getId() + "'");

        RestRequestRangesModel facetRangeModel = createRangesModel("content.size", "0", "200", "20");
        List<String> include = List.of("upper");
        facetRangeModel.setInclude(include);
        List<RestRequestRangesModel> ranges = List.of(facetRangeModel);
        query.setRanges(ranges);
        SearchResponse response = query(query);
        response.assertThat().entriesListIsNotEmpty();
        response.getContext().assertThat().field("facets").isNotEmpty();
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().getFirst();

        RestGenericBucketModel bucket = facetResponseModel.getBuckets().getFirst();
        bucket.assertThat().field("label").is("(20 - 40]");
        bucket.assertThat().field("filterQuery").is("content.size:<\"20\" TO \"40\"]");
        Map<String, String> metric = (Map<String, String>) bucket.getMetrics().getFirst().getValue();
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 2, "Unexpected count for first bucket.");
        Map<String, String> info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "20");
        assertEquals(info.get("end"), "40");
        assertNull(info.get("count"));
        assertEquals(info.get("startInclusive"), "false");
        assertEquals(info.get("endInclusive"), "true");

        bucket = facetResponseModel.getBuckets().get(1);
        bucket.assertThat().field("label").is("(40 - 120]");
        bucket.assertThat().field("filterQuery").is("content.size:<\"40\" TO \"120\"]");
        metric = (Map<String, String>) bucket.getMetrics().getFirst().getValue();
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 1, "Unexpected count for second bucket.");
        info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "40");
        assertEquals(info.get("end"), "120");
        assertEquals(info.get("startInclusive"), "false");
        assertEquals(info.get("endInclusive"), "true");

        bucket = facetResponseModel.getBuckets().get(2);
        bucket.assertThat().field("label").is("(120 - 200]");
        bucket.assertThat().field("filterQuery").is("content.size:<\"120\" TO \"200\"]");
        metric = (Map<String, String>) bucket.getMetrics().getFirst().getValue();
        assertEquals(Integer.valueOf(metric.get("count")).intValue(), 1, "Unexpected count for third bucket.");
        info = (Map<String, String>) bucket.getBucketInfo();
        assertEquals(info.get("start"), "120");
        assertEquals(info.get("end"), "200");
        assertEquals(info.get("startInclusive"), "false");
        assertEquals(info.get("endInclusive"), "true");
    }

    /**
     * Create a ranges model with the values given.
     *
     * @param field
     *            The field to facet on.
     * @param start
     *            The lowest facet value.
     * @param end
     *            The highest facet value.
     * @param gap
     *            The size of the buckets.
     * @return The facet ranges model.
     */
    private RestRequestRangesModel createRangesModel(String field, String start, String end, String gap)
    {
        RestRequestRangesModel facetRangeModel = new RestRequestRangesModel();
        facetRangeModel.setField(field);
        facetRangeModel.setStart(start);
        facetRangeModel.setEnd(end);
        facetRangeModel.setGap(gap);
        return facetRangeModel;
    }
}
