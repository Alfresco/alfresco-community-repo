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

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsParameters;
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
  
    static Map<String, String> languageMappings;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        languageMappings = new HashMap<String, String>();
        languageMappings.put("solr-alfresco", "alfresco");
        languageMappings.put("solr-fts-alfresco", "afts");
        languageMappings.put("solr-cmis", "cmis");
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testBuildStatsUrl() throws UnsupportedEncodingException
    {
        SolrQueryHTTPClient client = new SolrQueryHTTPClient();
        client.setLanguageMappings(languageMappings);
        StatsParameters params = getParameters();
        String url = client.buildStatsUrl(params, "http://localhost:8080/solr/alfresco/select", Locale.CANADA_FRENCH);
        assertNotNull(url);
        assertTrue(url.contains("locale=fr_CA"));
        assertTrue(url.contains("sort=contentsize"));
        assertTrue(url.contains("fq=ANCESTOR"));

    }
    
    @Test
    public void testBuildStatsBody() throws JSONException
    {
        SolrQueryHTTPClient client = new SolrQueryHTTPClient();
        StatsParameters params = getParameters();
        JSONObject body = client.buildStatsBody(params, "myTenant", Locale.US);
        assertNotNull(body);
        JSONArray tenant = body.getJSONArray("tenants");
        assertEquals("myTenant",tenant.get(0).toString());
        JSONArray locale = body.getJSONArray("locales");
        assertEquals("en_US",locale.get(0).toString());
        String query = body.getString("query");
        assertTrue(query.contains("TYPE:"));
        assertTrue(query.contains("{http://www.alfresco.org/model/content/1.0}content"));
    }
    
    private StatsParameters getParameters() {

        StringBuilder luceneQuery = new StringBuilder();
        luceneQuery.append(" +TYPE:\"" + ContentModel.TYPE_CONTENT + "\"");
        
        String filterQuery = "ANCESTOR:\"workspace://SpacesStore/a1c1a0a1-9d68-4912-b853-b3b277f31288\"";
        StatsParameters params = new StatsParameters(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, luceneQuery.toString(), filterQuery, false);
        params.addSort(new SortDefinition(SortDefinition.SortType.FIELD, "contentsize", false));
        params.addStatsParameter(StatsParameters.PARAM_FIELD, "contentsize");
        params.addStatsParameter(StatsParameters.PARAM_FACET, StatsParameters.FACET_PREFIX+ContentModel.PROP_CREATED.toString());
        params.addStatsParameter("Test1", StatsParameters.FACET_PREFIX+"author. .u");
        params.addStatsParameter("Test2", StatsParameters.FACET_PREFIX+"creator. .u");
        return params;
    }

}
