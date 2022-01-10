/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * REST API Tests for Action Definitions
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class SubstitutionSuggestionsRestApiTest extends BaseRMWebScriptTestCase
{
    /** URL for the REST APIs */
    private static final String RM_SUBSTITUTIONSUGGESTIONS_URL = "/api/rm/rm-substitutionsuggestions?fragment=month";

    /**
     * Test the REST API to retrieve the list of rm substitution suggestions
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmGetSubstitutionSuggestions() throws IOException, JSONException
    {
        // Send request
        Response response = sendRequest(new GetRequest(RM_SUBSTITUTIONSUGGESTIONS_URL), Status.STATUS_OK);

        // Check the content from the response
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json and check the data
        JSONObject contentAsJson = new JSONObject(contentAsString);
        JSONArray data = contentAsJson.getJSONArray("substitutions");
        assertNotNull(data);

        // Get the list of rm action definitions from the response and check it
        List<String> substitutionDefinitions = getSubstitutionDefinitions();
        List<String> rmSubstitutionDefinitions = new ArrayList<>();
        for (int i = 0; i < data.length(); i++)
        {
            String value = data.getString(i);
            assertNotNull(value);
            rmSubstitutionDefinitions.add(value);
        }
        assertTrue(rmSubstitutionDefinitions.containsAll(substitutionDefinitions));
        assertTrue(substitutionDefinitions.containsAll(rmSubstitutionDefinitions));
    }

    /**
     * Returns a (sub)list of dm action definitions
     *
     * @return A (sub)list of dm action definitions
     */
    private List<String> getSubstitutionDefinitions()
    {
        return Arrays.asList(new String[]
        {
            "date.month.number",
            "date.month.long",
            "date.month.short",
            "date.month",
            "date.day.month",
        });
    }
}
