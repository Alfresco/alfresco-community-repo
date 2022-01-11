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

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * REST API Test for Email mapping keys
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class EmailMapKeysRestApiTest extends BaseRMWebScriptTestCase
{
    /** URLs for the REST API */
    private static final String GET_EMAIL_MAP_KEYS_URL = "/api/rma/admin/emailmapkeys";

    /**
     * Tests the REST API to get the list of email mapping keys
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testGetCapabilitiesAction() throws IOException, JSONException
    {
        // Send request
        Response response = sendRequest(new GetRequest(GET_EMAIL_MAP_KEYS_URL), Status.STATUS_OK);

        // Check the content from the response
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json and check the data
        JSONObject contentAsJson = new JSONObject(contentAsString);
        JSONObject data = contentAsJson.getJSONObject("data");
        assertNotNull(data);

        // Get the email mapping keys and check them
        JSONArray dataSets = data.getJSONArray("emailmapkeys");
        assertNotNull(dataSets);

        // Check the number of email mapping keys
        assertTrue(dataSets.length() == 6);
    }
}
