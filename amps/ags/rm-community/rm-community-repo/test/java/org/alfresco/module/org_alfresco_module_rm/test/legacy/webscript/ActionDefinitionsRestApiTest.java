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
 * @author Tuna Aksoy
 * @since 2.1
 */
public class ActionDefinitionsRestApiTest extends BaseRMWebScriptTestCase
{
    /** URL for the REST APIs */
    private static final String RM_ACTIONDEFINITIONS_URL = "/api/rm/rm-actiondefinitions";    

    /**
     * Test the REST API to retrieve the list of rm action definitions
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmGetActionDefinitions() throws IOException, JSONException
    {
        // Send request
        Response response = sendRequest(new GetRequest(RM_ACTIONDEFINITIONS_URL), Status.STATUS_OK);

        // Check the content from the response
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json and check the data
        JSONObject contentAsJson = new JSONObject(contentAsString);
        JSONArray data = contentAsJson.getJSONArray("data");
        assertNotNull(data);

        // Get a (sub)list of available dm action definitions
        List<String> dmActionDefinitions = getDmActionDefinitions();

        // Get the list of rm action definitions from the response and check it
        List<String> rmActionDefinitions = new ArrayList<>();
        for (int i = 0; i < data.length(); i++)
        {
            String name = data.getJSONObject(i).getString("name");
            assertNotNull(name);
            rmActionDefinitions.add(name);
            assertFalse(dmActionDefinitions.contains(name));
        }
        assertTrue(rmActionDefinitions.containsAll(getRmActionDefinitions()));
    }

    /**
     * Returns a (sub)list of rm action definitions
     *
     * @return A (sub)list of rm action definitions
     */
    private List<String> getRmActionDefinitions()
    {
        return Arrays.asList(new String[]
        {
            "reject",
            "fileTo",
            "declareRecord"
        });
    }

    /**
     * Returns a (sub)list of dm action definitions
     *
     * @return A (sub)list of dm action definitions
     */
    private List<String> getDmActionDefinitions()
    {
        return Arrays.asList(new String[]
        {
            "check-in",
            "check-out",
            "mail",
            "move",
            "transform"
        });
    }
}
