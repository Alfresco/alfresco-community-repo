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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
/**
 * Validates that the SolrSQLJSONResultSet is accurately parsing the solr stream response.
 * @author Michael Suzuki
 *
 */
public class SolrSQLJSONResultSetTest
{
    @Test
    public void parseSQLResponse() throws JSONException
    {
        String response = "{\"result-set\":{\"docs\":[{\"SITE\":\"_REPOSITORY_\"},{\"SITE\":\"surf-config\"},{\"SITE\":\"swsdp\"},{\"RESPONSE_TIME\":96,\"EOF\":true}]}}";
        JSONObject json = new JSONObject(response);
        SolrSQLJSONResultSet ssjr = new SolrSQLJSONResultSet(json, null);
        Assert.assertNotNull(ssjr);
        Assert.assertNotNull(ssjr.getQueryTime());
        Assert.assertEquals(new Long(96), ssjr.getQueryTime());
        Assert.assertEquals(3, ssjr.getNumberFound());
        Assert.assertNotNull(ssjr.getSolrResponse());
        Assert.assertEquals(response, ssjr.getSolrResponse());
        JSONArray docs = ssjr.getDocs();
        Assert.assertNotNull(docs);
        Assert.assertNotNull(ssjr.getResultSetMetaData());
        
    }
    @Test
    public void parseSQLErrorResponse() throws JSONException
    {
        String response = "{\"result-set\":{\"docs\":[{\"EXCEPTION\":\"Column 'SIT1E' not found in any table\",\"EOF\":true,\"RESPONSE_TIME\":18943}]}}";
        try
        {
            JSONObject json = new JSONObject(response);
            SolrSQLJSONResultSet ssjr = new SolrSQLJSONResultSet(json, null);
            Assert.assertNull(ssjr);
        }
        catch (RuntimeException e)
        {
            Assert.assertNotNull(e);
            Assert.assertEquals("Unable to execute the query, error caused by: Column 'SIT1E' not found in any table", e.getMessage());
        }
    }
    
    /**
     * Validates that when a query is done against SearchService then it should state that it works only with
     * Insight Engine.
     * @throws JSONException
     */
    @Test
    public void parseInvalidInsightEngineResponse() throws JSONException
    {
        String response = "{\"result-set\":{\"docs\":[{\"EXCEPTION\":\"/sql handler only works in Solr Cloud mode\",\"EOF\":true}]}}";
        try
        {
            JSONObject json = new JSONObject(response);
            SolrSQLJSONResultSet ssjr = new SolrSQLJSONResultSet(json, null);
            Assert.assertNull(ssjr);
        }
        catch (RuntimeException e)
        {
            Assert.assertNotNull(e);
            Assert.assertEquals("Unable to execute the query, this API requires InsightEngine.", e.getMessage());
        }
    }
}
