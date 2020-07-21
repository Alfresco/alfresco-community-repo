/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search.impl.solr;

import static junit.framework.TestCase.assertEquals;
import static org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_PREFIX;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.forms.processor.node.MockClassAttributeDefinition;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.Interval;
import org.alfresco.service.cmr.search.IntervalParameters;
import org.alfresco.service.cmr.search.IntervalSet;
import org.alfresco.service.cmr.search.RangeParameters;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsRequestParameters;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.testing.category.LuceneTests;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Basic test of SolrQueryHTTPClient
 *
 * @author Gethin James
 * @since 5.0
 */
@Category(LuceneTests.class)
public class SolrQueryHTTPClientTest
{
    static SolrQueryHTTPClient client = new SolrQueryHTTPClient();
    static URLCodec encoder = new URLCodec();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Map<String, String> languageMappings = new HashMap<String, String>();
        languageMappings.put("solr-alfresco", "alfresco");
        languageMappings.put("solr-fts-alfresco", "afts");
        languageMappings.put("solr-cmis", "cmis");

        NamespaceDAO namespaceDAO = mock(NamespaceDAO.class);
        DictionaryService dictionaryService = mock(DictionaryService.class);

        when(namespaceDAO.getPrefixes()).thenReturn(Arrays.asList(CONTENT_MODEL_PREFIX, "exif"));
        when(namespaceDAO.getNamespaceURI(anyString())).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        when(dictionaryService.getProperty(notNull(QName.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            QName qName = (QName) args[0];
            if (qName.getLocalName().contains("created"))
            {
                return MockClassAttributeDefinition.mockPropertyDefinition(qName, DataTypeDefinition.DATE);
            }
            else
            {
                return MockClassAttributeDefinition.mockPropertyDefinition(qName, DataTypeDefinition.ANY);
            }

        });

        client.setLanguageMappings(languageMappings);
        client.setDictionaryService(dictionaryService);
        client.setNamespaceDAO(namespaceDAO);

        //required for init() but not used.
        client.setNodeService(mock(NodeService.class));
        client.setTenantService(mock(TenantService.class));
        client.setPermissionService(mock(PermissionService.class));
        client.setStoreMappings(Collections.emptyList());
        client.setRepositoryState(mock(RepositoryState.class));
        client.init();
    }

    @Test
    public void testBuildStatsUrl() throws UnsupportedEncodingException
    {
        StatsParameters params = getParameters();
        String url = client.buildStatsUrl(params, "http://localhost:8080/solr/alfresco/select", Locale.CANADA_FRENCH, null);
        assertNotNull(url);
        assertTrue(url.contains("locale=fr_CA"));
        assertTrue(url.contains("sort=contentsize"));
        assertTrue(url.contains("fq=ANCESTOR"));

    }

    @Test
    public void testBuildStatsBody() throws JSONException
    {

        StatsParameters params = getParameters();
        JSONObject body = client.buildStatsBody(params, "myTenant", Locale.US);
        assertNotNull(body);
        JSONArray tenant = body.getJSONArray("tenants");
        assertEquals("myTenant", tenant.get(0).toString());
        JSONArray locale = body.getJSONArray("locales");
        assertEquals("en_US", locale.get(0).toString());
        String query = body.getString("query");
        assertTrue(query.contains("TYPE:"));
        assertTrue(query.contains("{http://www.alfresco.org/model/content/1.0}content"));
    }

    private StatsParameters getParameters()
    {

        StringBuilder luceneQuery = new StringBuilder();
        luceneQuery.append(" +TYPE:\"" + ContentModel.TYPE_CONTENT + "\"");
        String filterQuery = "ANCESTOR:\"workspace://SpacesStore/a1c1a0a1-9d68-4912-b853-b3b277f31288\"";
        StatsParameters params = new StatsParameters(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, luceneQuery.toString(), filterQuery, false);
        params.addSort(new SortDefinition(SortDefinition.SortType.FIELD, "contentsize", false));
        params.addStatsParameter(StatsParameters.PARAM_FIELD, "contentsize");
        params.addStatsParameter(StatsParameters.PARAM_FACET, StatsParameters.FACET_PREFIX + ContentModel.PROP_CREATED.toString());
        params.addStatsParameter("Test1", StatsParameters.FACET_PREFIX + "author. .u");
        params.addStatsParameter("Test2", StatsParameters.FACET_PREFIX + "creator. .u");
        return params;
    }


    @Test
    public void testBuildTimezone() throws UnsupportedEncodingException
    {
        SearchParameters params = new SearchParameters();
        params.setTimezone("");
        StringBuilder urlBuilder = new StringBuilder();
        client.buildUrlParameters(params, false, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertFalse(url.contains("&TZ"));

        params.setTimezone("bob");
        urlBuilder = new StringBuilder();
        client.buildUrlParameters(params, false, encoder, urlBuilder);
        url = urlBuilder.toString();

        //Timezone formats are not validated here so its just passing a string.
        assertTrue(url.contains("&TZ=bob"));;
    }

    @Test
    public void testBuildHighlightQuery() throws UnsupportedEncodingException
    {
        SearchParameters params = new SearchParameters();
        params.setSearchTerm("bob");
        StringBuilder urlBuilder = new StringBuilder();
        client.buildUrlParameters(params, false, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertFalse(url.contains("&hl"));

        urlBuilder = new StringBuilder();
        GeneralHighlightParameters highlightParameters = new GeneralHighlightParameters(null, null, null, null, null, null, null, null);
        params.setHighlight(highlightParameters);
        client.buildUrlParameters(params, true, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertTrue(url.contains("&hl=true"));
        assertTrue(url.contains("&hl.q=bob"));

        urlBuilder = new StringBuilder();
        highlightParameters = new GeneralHighlightParameters(5, 10, false, "{", "}", 20, true, null);
        params.setHighlight(highlightParameters);
        client.buildUrlParameters(params, false, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertTrue(url.contains("&hl=true"));
        assertTrue(url.contains("&hl.q=bob"));
        assertTrue(url.contains("&hl.snippets=5"));
        assertTrue(url.contains("&hl.fragsize=10"));
        assertTrue(url.contains("&hl.maxAnalyzedChars=20"));
        assertTrue(url.contains("&hl.mergeContiguous=false"));
        assertTrue(url.contains("&hl.usePhraseHighlighter=true"));

        assertTrue(url.contains("&hl.simple.pre=" + encoder.encode("{", "UTF-8")));
        assertTrue(url.contains("&hl.simple.post=" + encoder.encode("}", "UTF-8")));

        List<FieldHighlightParameters> fields = Arrays.asList(new FieldHighlightParameters(null, null, null, null, null, null));
        urlBuilder = new StringBuilder();
        highlightParameters = new GeneralHighlightParameters(5, 10, false, "{", "}", 20, true, fields);
        params.setHighlight(highlightParameters);

        try
        {
            client.buildUrlParameters(params, false, encoder, urlBuilder);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            assertNotNull("no fieldname specfied so invalid", iae);
        }

        fields = Arrays.asList(new FieldHighlightParameters("desc", 50, 100, false, "@", "#"),
                    new FieldHighlightParameters("title", 55, 105, true, "*", "¿"));
        urlBuilder = new StringBuilder();
        highlightParameters = new GeneralHighlightParameters(5, 10, false, "{", "}", 20, true, fields);
        params.setHighlight(highlightParameters);
        client.buildUrlParameters(params, false, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertTrue(url.contains("&hl=true"));
        assertTrue(url.contains("&hl.fl=" + encoder.encode("desc,title", "UTF-8")));
        assertTrue(url.contains("&f.desc.hl.snippets=50"));
        assertTrue(url.contains("&f.title.hl.snippets=55"));
        assertTrue(url.contains("&f.desc.hl.fragsize=100"));
        assertTrue(url.contains("&f.title.hl.fragsize=105"));
        assertTrue(url.contains("&f.desc.hl.mergeContiguous=false"));
        assertTrue(url.contains("&f.title.hl.mergeContiguous=true"));
        assertTrue(url.contains("&f.desc.hl.simple.pre=" + encoder.encode("@", "UTF-8")));
        assertTrue(url.contains("&f.desc.hl.simple.post=" + encoder.encode("#", "UTF-8")));
        assertTrue(url.contains("&f.title.hl.simple.pre=" + encoder.encode("*", "UTF-8")));
        assertTrue(url.contains("&f.title.hl.simple.post=" + encoder.encode("¿", "UTF-8")));

    }

    @Test
    public void testBuildFacetIntervalQuery() throws UnsupportedEncodingException
    {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SearchParameters params = new SearchParameters();
        params.setSearchTerm("bob");

        IntervalSet intervalSet = new IntervalSet("8", "12", null, null, null);
        params.setInterval(new IntervalParameters(new HashSet(Arrays.asList(intervalSet)), null));
        StringBuilder urlBuilder = new StringBuilder();
        client.buildFacetIntervalParameters(params, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains(encoder.encode("{!afts}[8,12]", "UTF-8")));

        intervalSet = new IntervalSet("1", "10", "numbers", false, true);
        params.setInterval(new IntervalParameters(new HashSet(Arrays.asList(intervalSet)), null));
        urlBuilder = new StringBuilder();
        client.buildFacetIntervalParameters(params, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains(encoder.encode("{!afts key=numbers}(1,10]", "UTF-8")));

        List<Interval> intervalList = Arrays.asList(new Interval("cm:price", "Price", null),
                    new Interval("cm:created", "Created", new HashSet(Arrays.asList(new IntervalSet("2015", "2016-12", "special", false, true)))));
        params.setInterval(new IntervalParameters(new HashSet(Arrays.asList(intervalSet)), intervalList));
        urlBuilder = new StringBuilder();
        client.buildFacetIntervalParameters(params, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains(encoder.encode("{!afts key=numbers}(1,10]", "UTF-8")));

        assertTrue(url.contains(encoder.encode("{!key=Price}cm:price", "UTF-8")));
        assertTrue(url.contains(encoder.encode("{!key=Created}cm:created", "UTF-8")));
        assertTrue(url.contains(encoder.encode("f.cm:created.facet.interval.set", "UTF-8")));
        assertTrue(url.contains(encoder.encode("{!afts key=numbers}", "UTF-8")));
        assertTrue(url.contains(encoder.encode("(2015-12-31T23:59:59.999Z", "UTF-8")));
        assertTrue(url.contains(encoder.encode("2016-12-31T23:59:59.999Z]", "UTF-8")));

        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    public void testBuildFieldFacets() throws UnsupportedEncodingException
    {
        SearchParameters params = new SearchParameters();
        params.setSearchTerm("bob");

        SearchParameters.FieldFacet prefixff = new SearchParameters.FieldFacet("{!afts something=right}modifier");
        SearchParameters.FieldFacet ff = new SearchParameters.FieldFacet("creator");
        params.addFieldFacet(prefixff);
        params.addFieldFacet(ff);

        StringBuilder urlBuilder = new StringBuilder();
        client.buildFacetParameters(params, false, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("facet.field=creator"));
        assertTrue(url.contains("f.creator.facet.limit=100"));
        assertTrue(url.contains("facet.field=" + encoder.encode("{!afts something=right}modifier", "UTF-8")));
        assertTrue(url.contains("f.modifier.facet.limit=100"));

        prefixff.setLabel("myLabel");
        ff.setLabel("yourLabel");

        urlBuilder = new StringBuilder();
        client.buildFacetParameters(params, false, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("facet.field=" + encoder.encode("{!afts key=yourLabel}creator", "UTF-8")));
        assertTrue(url.contains("f.creator.facet.limit=100"));
        assertTrue(url.contains("facet.field=" + encoder.encode("{!afts key=myLabel something=right}modifier", "UTF-8")));
        assertTrue(url.contains("f.modifier.facet.limit=100"));

        prefixff.setExcludeFilters(Arrays.asList("x", "y"));
        ff.setExcludeFilters(Arrays.asList("B"));

        urlBuilder = new StringBuilder();
        client.buildFacetParameters(params, false, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("facet.field=" + encoder.encode("{!afts ex=B key=yourLabel}creator", "UTF-8")));
        assertTrue(url.contains("f.creator.facet.limit=100"));
        assertTrue(url.contains("facet.field=" + encoder.encode("{!afts ex=x,y key=myLabel something=right}modifier", "UTF-8")));
        assertTrue(url.contains("f.modifier.facet.limit=100"));

        prefixff.setField("bill");
        prefixff.setExcludeFilters(Collections.emptyList());
        ff.setField("{!afts}ben");
        ff.setLabel(null);

        urlBuilder = new StringBuilder();
        client.buildFacetParameters(params, false, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("facet.field=" + encoder.encode("{!afts ex=B}ben", "UTF-8")));
        assertTrue(url.contains("f.ben.facet.limit=100"));
        assertTrue(url.contains("facet.field=" + encoder.encode("{!afts key=myLabel}bill", "UTF-8")));
        assertTrue(url.contains("f.bill.facet.limit=100"));

    }

    @Test
    public void testBuildStats() throws UnsupportedEncodingException
    {
        SearchParameters params = new SearchParameters();
        params.setSearchTerm("bob");
        params.setStats(Arrays.asList(
                    new StatsRequestParameters("created", null, null, null, null,null, null, null, null,
                                null, null, null, null,null, null,  null),
                    new StatsRequestParameters("cm:name", "statLabel",
                    Arrays.asList(2.4f, 99.9f),null, null, false, null,false, null, false, null, true, true,
                    true, 0.5f, Arrays.asList("excludeme"))));

        StringBuilder urlBuilder = new StringBuilder();
        client.buildStatsParameters(params, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&stats=true"));
        assertTrue(url.contains("stats.field=" + encoder.encode(
                   "{! countDistinct=false distinctValues=false min=true max=true sum=true count=true missing=true sumOfSquares=true mean=true stddev=true}created", "UTF-8")));
        assertTrue(url.contains("stats.field=" + encoder.encode(
                   "{! ex=excludeme tag=statLabel key=statLabel percentiles='2.4,99.9' cardinality=0.5 countDistinct=true distinctValues=true min=true max=true sum=false count=true missing=false sumOfSquares=true mean=false stddev=true}cm:name", "UTF-8")));

    }

    @Test
    public void testBuildPivots() throws UnsupportedEncodingException
    {
        SearchParameters params = new SearchParameters();
        params.setSearchTerm("bob");
        params.addPivots(Arrays.asList("creator"));
        params.setStats(Arrays.asList(
                    new StatsRequestParameters("created", "piv1", null, null, null,null, null, null, null,
                                null, null, null, null,null, null,  null)
                    ));
        List<RangeParameters> ranges = new ArrayList<RangeParameters>();
        ranges.add(new RangeParameters("content.size","0","1000000", "10000", true, Collections.emptyList(), Collections.emptyList(), "csize",null));
        params.setRanges(ranges);

        StringBuilder urlBuilder = new StringBuilder();

        client.buildPivotParameters(params, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("facet.pivot=creator"));

        params.addPivots(Arrays.asList("cm:name", "piv1", "csize"));

        urlBuilder = new StringBuilder();
        client.buildPivotParameters(params, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("facet.pivot="+ encoder.encode("creator", "UTF-8")));
        assertTrue(url.contains("facet.pivot="+ encoder.encode("{! stats=piv1 range=csize}cm:name", "UTF-8")));
    }

    @Test
    public void testBuildRange() throws UnsupportedEncodingException
    {
        SearchParameters params = new SearchParameters();
        params.setSearchTerm("A*");
        List<RangeParameters> ranges = new ArrayList<RangeParameters>();
        List<String>includes = new ArrayList<String>();
        includes.add("upper");
        includes.add("outer");
        List<String> other = new ArrayList<String>();
        other.add("before");
        other.add("between");
        ranges.add(new RangeParameters("content.size", "0", "1000000", "10000", true, other, includes, null, null));
        params.setRanges(ranges);
        StringBuilder urlBuilder = new StringBuilder();
        client.buildRangeParameters(params, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("&facet.range=content.size"));
        assertTrue(url.contains("&f.content.size.facet.range.start=0"));
        assertTrue(url.contains("&f.content.size.facet.range.end=1000000"));
        assertTrue(url.contains("&f.content.size.facet.range.gap=10000"));
        assertTrue(url.contains("&f.content.size.facet.range.include=upper"));
        assertTrue(url.contains("&f.content.size.facet.range.include=outer"));
        assertTrue(url.contains("&f.content.size.facet.range.other=before"));
        assertTrue(url.contains("&f.content.size.facet.range.other=between"));
        assertTrue(url.contains("&f.content.size.facet.range.hardend=true"));

        List<String> filters = new ArrayList<String>();
        filters.add("ex1");

        ranges.clear();
        ranges.add(new RangeParameters("content.size", "0", "1000000", "10000", true, Collections.emptyList(), Collections.emptyList(), "doc", filters));
        params.setRanges(ranges);
        urlBuilder = new StringBuilder();
        client.buildRangeParameters(params, encoder, urlBuilder);
        String url2 = urlBuilder.toString();
        assertTrue(url2.contains("&facet=true"));
        assertTrue(url2.contains("&facet.range="+encoder.encode("{!tag=doc }", "UTF-8")+"content.size"));
        assertTrue(url2.contains("&f.content.size.facet.range.start=0"));
        assertTrue(url2.contains("&f.content.size.facet.range.end=1000000"));
        assertTrue(url2.contains("&f.content.size.facet.range.gap=10000"));
        assertFalse(url2.contains("&f.content.size.facet.range.include=upper"));
        assertFalse(url2.contains("&f.content.size.facet.range.include=outer"));
        assertFalse(url2.contains("&f.content.size.facet.range.other=before"));
        assertTrue(url2.contains("&f.content.size.facet.range.hardend=true"));
        assertTrue(url2.contains("&facet.range={!ex=ex1}content.size"));
    }
    @Test
    public void testBuildMulitRange() throws UnsupportedEncodingException
    {
        SearchParameters params = new SearchParameters();
        params.setSearchTerm("A*");
        List<RangeParameters> ranges = new ArrayList<RangeParameters>();
        List<String>includes = new ArrayList<String>();
        includes.add("upper");
        includes.add("outer");
        List<String>includes2 = new ArrayList<String>();
        includes2.add("lower");
        List<String> other = new ArrayList<String>();
        other.add("before");
        ranges.add(new RangeParameters("content.size", "0", "1000000", "10000", true, other, includes, null, null));
        ranges.add(new RangeParameters("created", "2015-09-29T10:45:15.729Z", "2016-09-29T10:45:15.729Z", "+100DAY", true, other, includes2, null, null));
        params.setRanges(ranges);
        StringBuilder urlBuilder = new StringBuilder();
        client.buildRangeParameters(params, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("&facet.range=content.size"));
        assertTrue(url.contains("&f.content.size.facet.range.start=0"));
        assertTrue(url.contains("&f.content.size.facet.range.end=1000000"));
        assertTrue(url.contains("&f.content.size.facet.range.gap=10000"));
        assertTrue(url.contains("&f.content.size.facet.range.include=upper"));
        assertTrue(url.contains("&f.content.size.facet.range.include=outer"));
        assertTrue(url.contains("&f.content.size.facet.range.hardend=true"));
        assertTrue(url.contains("&facet.range=created"));
        assertTrue(url.contains("&f.created.facet.range.start=2015-09-29T10%3A45%3A15.729Z"));
        assertTrue(url.contains("&f.created.facet.range.end=2016-09-29T10%3A45%3A15.729Z"));
        assertTrue(url.contains("&f.created.facet.range.gap=%2B100DAY"));
        assertTrue(url.contains("&f.created.facet.range.other=before"));
        assertTrue(url.contains("&f.created.facet.range.include=lower"));
        assertTrue(url.contains("&f.created.facet.range.hardend=true"));
    }
    @Test
    public void testBuildRangeDate() throws UnsupportedEncodingException
    {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SearchParameters params = new SearchParameters();
        params.setSearchTerm("A*");
        List<RangeParameters> ranges = new ArrayList<RangeParameters>();
        ranges.add(new RangeParameters("created", "2015", "2016", "+1MONTH", true, Collections.emptyList(), Collections.emptyList(), null, null));
        params.setRanges(ranges);
        StringBuilder urlBuilder = new StringBuilder();
        client.buildRangeParameters(params, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("&facet.range=created"));
        assertTrue(url.contains("&f.created.facet.range.start=2015-01-01T00%3A00%3A00.000Z"));
        assertTrue(url.contains("&f.created.facet.range.end=2016-12-31T23%3A59%3A59.999Z"));
        assertTrue(url.contains("&f.created.facet.range.gap=%2B1MONTH"));

        TimeZone.setDefault(defaultTimeZone);
    }

}