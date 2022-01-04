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
import java.text.MessageFormat;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * REST API Test for Capabilities
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class CapabilitiesRestApiTest extends BaseRMWebScriptTestCase
{
    /** URLs for the REST API */
    private static final String GET_CAPABILITIES_URL = "/api/node/{0}/{1}/{2}/capabilities?";

    /**
     * Tests the REST API to get the list of capabilities
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testGetCapabilitiesAction() throws IOException, JSONException
    {
    	String baseURL = MessageFormat.format(GET_CAPABILITIES_URL, 
    								   	      filePlan.getStoreRef().getProtocol(), 
    								   	      filePlan.getStoreRef().getIdentifier(),
    								   	      filePlan.getId());
    	
        // Format url and send request
        String getUrl = String.format(baseURL + "includeAll=%s", true);
        Response getResponse = sendRequest(new GetRequest(getUrl), Status.STATUS_OK);

        // Check the content from the response
        String getContentAsString = getResponse.getContentAsString();
        assertNotNull(getContentAsString);

        System.out.println(getContentAsString);
        
        // Convert the response to json and check the data
        JSONObject getContentAsJson = new JSONObject(getContentAsString);
        JSONObject getData = getContentAsJson.getJSONObject("data");
        assertNotNull(getData);

        // Get the capabilities and check them
        JSONArray getDataSets = getData.getJSONArray("capabilities");
        assertNotNull(getDataSets);

        // Format url and send another request with different parameter
        getUrl = String.format(baseURL + "grouped=%s", true);
        getResponse = sendRequest(new GetRequest(getUrl), Status.STATUS_OK);

        // Check the content from the response
        getContentAsString = getResponse.getContentAsString();
        assertNotNull(getContentAsString);

        // If both parameters are specified the result should be the same with only specifying the "grouped" parameter
        getUrl = String.format(baseURL + "includeAll=%s&amp;grouped=%s", true, true);
        getResponse = sendRequest(new GetRequest(getUrl), Status.STATUS_OK);
        getContentAsString.equalsIgnoreCase(getResponse.getContentAsString());

        // Convert the response to json and check the data
        getContentAsJson = new JSONObject(getContentAsString);
        getData = getContentAsJson.getJSONObject("data");
        assertNotNull(getData);

        // Get the grouped capabilities and check them
        getDataSets = getData.getJSONArray("groupedCapabilities");
        assertNotNull(getDataSets);

        // Check the JSON structure
        int length = getDataSets.length();
        if (length > 0)
        {
            for (int i = 0; i < length; i++)
            {
                JSONObject jsonObject = getDataSets.getJSONObject(i);
                String key = (String) jsonObject.keys().next();
                JSONObject value = jsonObject.getJSONObject(key);
                assertNotNull(value.getString("groupTitle"));
                assertNotNull(value.getJSONObject("capabilities"));
            }
        }
    }
}
