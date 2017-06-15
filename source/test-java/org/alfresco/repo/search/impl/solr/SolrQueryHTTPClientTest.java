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
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.forms.processor.node.MockClassAttributeDefinition;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.Interval;
import org.alfresco.service.cmr.search.IntervalParameters;
import org.alfresco.service.cmr.search.IntervalSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Basic test of SolrQueryHTTPClient
 *
 * @author Gethin James
 * @since 5.0
 */
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
        params.setInterval(new IntervalParameters(Arrays.asList(intervalSet), null));
        StringBuilder urlBuilder = new StringBuilder();
        client.buildFacetIntervalParameters(params, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains(encoder.encode("{!afts}[8,12]", "UTF-8")));

        intervalSet = new IntervalSet("1", "10", "numbers", false, true);
        params.setInterval(new IntervalParameters(Arrays.asList(intervalSet), null));
        urlBuilder = new StringBuilder();
        client.buildFacetIntervalParameters(params, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains(encoder.encode("{!afts key=numbers}(1,10]", "UTF-8")));

        List<Interval> intervalList = Arrays.asList(new Interval("cm:price", "Price", null),
                    new Interval("cm:created", "Created", Arrays.asList(new IntervalSet("2015", "2016-12", "special", false, true))));
        params.setInterval(new IntervalParameters(Arrays.asList(intervalSet), intervalList));
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
    public void testBuildPivots() throws UnsupportedEncodingException
    {
        SearchParameters params = new SearchParameters();
        params.setSearchTerm("bob");
        params.addPivot("creator");

        StringBuilder urlBuilder = new StringBuilder();

        client.buildPivotParameters(params, encoder, urlBuilder);
        String url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("facet.pivot=creator"));

        params.addPivot("cm:name");
        params.addPivot("{!stats=piv1}cat");

        urlBuilder = new StringBuilder();
        client.buildPivotParameters(params, encoder, urlBuilder);
        url = urlBuilder.toString();
        assertNotNull(url);
        assertTrue(url.contains("&facet=true"));
        assertTrue(url.contains("facet.pivot="+ encoder.encode("creator,cm:name,{!stats=piv1}cat", "UTF-8")));
    }

}