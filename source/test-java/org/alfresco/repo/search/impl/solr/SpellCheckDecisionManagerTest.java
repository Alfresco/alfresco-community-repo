/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.search.impl.solr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.*;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

/**
 * This class contains tests for the class <code>{@link SpellCheckDecisionManager}</code>.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SpellCheckDecisionManagerTest
{
    private static final String RESOURCE_PREFIX = "org/alfresco/repo/search/impl/spellcheck/";
    private static final String SEARCH_REQUEST_URL = "/solr4/alfresco/afts?wt=json&fl=DBID%2Cscore&rows=251&df=keywords&start=0&locale=en&alternativeDic=DEFAULT_DICTIONARY&fq=%7B%21afts%7DAUTHORITY_FILTER_FROM_JSON&fq=%7B%21afts%7DTENANT_FILTER_FROM_JSON";

    /**
     * Test Collation. The {@code isCollate} method returns true, when the original query contains one or
     * more misspelled terms leading to 0 hit and if spell-suggestion is available
     * and collation's hits are greater than 0, the class should modify the
     * original search query to replace the misspelled term with the suggested term.
     *
     * @throws Exception
     */
    @Test
    public void testCollation() throws Exception
    {
        final String searchTerm = "alfrezco";

        JSONObject resultJson = getSearchResponseAsJson("searchInsteadFor.json");

        JSONObject requestBody = createJsonSearchRequest(searchTerm);
        String spellCheckParam = getSpellCheckParam(searchTerm);

        SpellCheckDecisionManager manager = new SpellCheckDecisionManager(resultJson, SEARCH_REQUEST_URL
                    + spellCheckParam, requestBody, spellCheckParam);

        assertTrue(manager.isCollate());
        assertEquals(SEARCH_REQUEST_URL, manager.getUrl());

        JSONObject searchedForJsonObject = manager.getSpellCheckJsonValue();
        assertEquals("alfresco", searchedForJsonObject.getString("searchInsteadFor"));

        // Check that the request query has been modified with the suggested term
        assertEquals(createJsonSearchRequest("alfresco").getString("query"), requestBody.getString("query"));

        // Check that the number of found docs (original query) is 0 and the collation hits are greater than 0
        long numberFound = getOrigQueryHit(resultJson);
        assertEquals(0L, numberFound);

        long collationHit = getCollationHit(resultJson);
        assertTrue(collationHit > 0L);
    }

    /**
     * Test didYouMean. If the original query resulting in a few hits;
     * suggestions are available and have more hits, the class should only
     * return the suggested terms, without modifying the query or setting the
     * collation flag to true.
     * 
     * @throws Exception
     */
    @Test
    public void testDidYouMean() throws Exception
    {
        final String searchTerm = "londn";

        JSONObject resultJson = getSearchResponseAsJson("didYouMean.json");

        JSONObject requestBody = createJsonSearchRequest(searchTerm);
        String spellCheckParam = getSpellCheckParam(searchTerm);

        SpellCheckDecisionManager manager = new SpellCheckDecisionManager(resultJson, SEARCH_REQUEST_URL
                    + spellCheckParam, requestBody, spellCheckParam);

        assertFalse(manager.isCollate());
        assertEquals(SEARCH_REQUEST_URL + spellCheckParam, manager.getUrl());

        JSONObject didYouMeanJsonObject = manager.getSpellCheckJsonValue();
        JSONArray jsonArray = didYouMeanJsonObject.getJSONArray("didYouMean");
        assertEquals(2, jsonArray.length());

        String[] suggestedTerms = { jsonArray.getString(0), jsonArray.getString(1) };
        Arrays.sort(suggestedTerms);

        assertEquals("login", suggestedTerms[0]);
        assertEquals("london", suggestedTerms[1]);

        // Check that the request query has NOT been modified with the suggested term
        assertEquals(createJsonSearchRequest(searchTerm).getString("query"), requestBody.getString("query"));

        // Check that the number of found docs (original query) is less than collation hits
        long numberFound = getOrigQueryHit(resultJson);
        long collationHit = getCollationHit(resultJson);

        assertTrue(collationHit > numberFound);
    }

    /**
     * Test no suggestions. Query contains a term that does not exist in the
     * index and also no suggestions are available. In this case, the class can't do anything.
     *
     * @throws Exception
     */
    @Test
    public void testNoSuggestions() throws Exception
    {
        final String searchTerm = "gibberishtextttttttttt";

        JSONObject resultJson = getSearchResponseAsJson("noSuggestions.json");

        JSONObject requestBody = createJsonSearchRequest(searchTerm);
        String spellCheckParam = getSpellCheckParam(searchTerm);

        SpellCheckDecisionManager manager = new SpellCheckDecisionManager(resultJson, SEARCH_REQUEST_URL
                    + spellCheckParam, requestBody, spellCheckParam);

        assertFalse(manager.isCollate());
        assertEquals(SEARCH_REQUEST_URL + spellCheckParam, manager.getUrl());

        JSONObject noSuggestionsJsonObject = manager.getSpellCheckJsonValue();
        assertEquals(0, noSuggestionsJsonObject.length());

        // Check that the request query has NOT been modified with the suggested term
        assertEquals(createJsonSearchRequest(searchTerm).getString("query"), requestBody.getString("query"));
        
        long numberFound = getOrigQueryHit(resultJson);
        assertEquals(0L, numberFound);

        long collationHit = getCollationHit(resultJson);
        assertEquals(0L, collationHit);
    }

    /**
     * Query contains a correctly spelled term; suggestions are available, but
     * the collation's hits are less than or equal to the original query term hits. In this
     * case, the class should not do anything
     *
     * @throws Exception
     */
    @Test
    public void testCorrectlySpelledTerm() throws Exception
    {
        final String searchTerm = "london";

        JSONObject resultJson = getSearchResponseAsJson("correctlySpelledTermQuery.json");

        JSONObject requestBody = createJsonSearchRequest(searchTerm);
        String spellCheckParam = getSpellCheckParam(searchTerm);

        SpellCheckDecisionManager manager = new SpellCheckDecisionManager(resultJson, SEARCH_REQUEST_URL
                    + spellCheckParam, requestBody, spellCheckParam);

        assertFalse(manager.isCollate());
        assertEquals(SEARCH_REQUEST_URL + spellCheckParam, manager.getUrl());

        JSONObject noSuggestionsJsonObject = manager.getSpellCheckJsonValue();
        assertEquals(0, noSuggestionsJsonObject.length());

        // Check that the request query has NOT been modified with the suggested term
        assertEquals(createJsonSearchRequest(searchTerm).getString("query"), requestBody.getString("query"));
        
        // Check that the number of found docs (original query) is greater than or equal to the collation hits
        long numberFound = getOrigQueryHit(resultJson);
        long collationHit = getCollationHit(resultJson);
        
        
        assertTrue(numberFound >= collationHit);
    }

    public JSONObject getSearchResponseAsJson(String jsonResponse) throws FileNotFoundException, JSONException
    {
        URL url = SpellCheckDecisionManagerTest.class.getClassLoader().getResource(RESOURCE_PREFIX + jsonResponse);
        if (url == null)
        {
            fail("Cannot get the resource: " + jsonResponse);
        }
        
        Reader reader = new FileReader(ResourceUtils.getFile(url));
        JSONObject resultJson = new JSONObject(new JSONTokener(reader));
        return resultJson;
    }

    public JSONObject createJsonSearchRequest(String searchTerm) throws JSONException
    {
        String requestStr = "{"
                    + "\"queryConsistency\" : \"DEFAULT\","
                    + "\"textAttributes\" : [],"
                    + "\"allAttributes\" : [],"
                    + "\"templates\" : [{"
                    + "\"template\" : \"%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT TAG)\","
                    + "\"name\" : \"keywords\""
                    + "}"
                    + "],"
                    + "\"authorities\" : [\"GROUP_EVERYONE\", \"ROLE_ADMINISTRATOR\", \"ROLE_AUTHENTICATED\", \"admin\"],"
                    + "\"tenants\" : [\"\"],"
                    + "\"query\" : \"("
                    + searchTerm
                    + "  AND (+TYPE:\\\"cm:content\\\" OR +TYPE:\\\"cm:folder\\\")) AND -TYPE:\\\"cm:thumbnail\\\" AND -TYPE:\\\"cm:failedThumbnail\\\" AND -TYPE:\\\"cm:rating\\\" AND -TYPE:\\\"st:site\\\" AND -ASPECT:\\\"st:siteContainer\\\" AND -ASPECT:\\\"sys:hidden\\\" AND -cm:creator:system AND -QNAME:comment\\\\-*\","
                    + "\"locales\" : [\"en\"],"
                    + "\"defaultNamespace\" : \"http://www.alfresco.org/model/content/1.0\","
                    + "\"defaultFTSFieldOperator\" : \"AND\"," + "\"defaultFTSOperator\" : \"AND\"" + "}";

        InputStream is = new ByteArrayInputStream(requestStr.getBytes());
        Reader reader = new BufferedReader(new InputStreamReader(is));
        JSONObject json = new JSONObject(new JSONTokener(reader));
        return json;
    }

    private String getSpellCheckParam(String searchTerm)
    {
        return "&spellcheck.q=" + searchTerm + "&spellcheck=true";
    }

    private long getOrigQueryHit(JSONObject resultJson) throws JSONException
    {
        JSONObject response = resultJson.getJSONObject("response");
        long numberFound = response.getLong("numFound");
        return numberFound;

    }

    private long getCollationHit(JSONObject resultJson) throws JSONException
    {
        JSONObject spellcheck = resultJson.getJSONObject("spellcheck");
        JSONArray suggestions = spellcheck.getJSONArray("suggestions");

        for (int key = 0, value = 1, length = suggestions.length(); value < length; key += 2, value += 2)
        {
            String jsonName = suggestions.getString(key);

            if ("collation".equals(jsonName))
            {
                JSONObject valueJsonObject = suggestions.getJSONObject(value);
                long collationHit = valueJsonObject.getLong("hits");
                return collationHit;
            }
        }

        return 0;
    }
}