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
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class EmailMapScriptTest extends BaseRMWebScriptTestCase
{
    /** URLs for the REST APIs */
    public final static String URL_RM_EMAILMAP = "/api/rma/admin/emailmap";
    public final static String URL_RM_EMAILMAP_DELETE = "/api/rma/admin/emailmap/%s/%s";

    /** Constant for the content type */
    private static final String APPLICATION_JSON = "application/json";

    /**
     * Tests the REST APIs for a custom mapping
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testEmailMap() throws IOException, JSONException
    {
        /** Test GET */
        Response getResponse = sendRequest(new GetRequest(URL_RM_EMAILMAP), Status.STATUS_OK);

        JSONObject getResponseContent = new JSONObject(getResponse.getContentAsString());
        JSONObject getData = getResponseContent.getJSONObject("data");
        JSONArray getMappings = getData.getJSONArray("mappings");
        assertTrue(getMappings.length() == 20);

        /** Test POST */
        JSONObject newMapping = new JSONObject();
        newMapping.put("from", "messageTo");
        newMapping.put("to", "rmc:Wibble");

        Response postResponse = sendRequest(new PostRequest(URL_RM_EMAILMAP, newMapping.toString(), APPLICATION_JSON), Status.STATUS_OK);
        JSONObject postResponseContent = new JSONObject(postResponse.getContentAsString());
        JSONObject postData = postResponseContent.getJSONObject("data");
        JSONArray postMappings = postData.getJSONArray("mappings");

        assertTrue(postMappings.length() == 21);
        assertTrue(existsMapping(postMappings));

        /** Test DELETE */
        Response deleteResponse = sendRequest(new DeleteRequest(String.format(URL_RM_EMAILMAP_DELETE, "messageTo", "rmc:Wibble")), Status.STATUS_OK);
        JSONObject deleteResponseContent = new JSONObject(deleteResponse.getContentAsString());
        JSONObject deleteData = deleteResponseContent.getJSONObject("data");
        JSONArray deleteMappings = deleteData.getJSONArray("mappings");

        assertTrue(deleteMappings.length() == 20);
        assertFalse(existsMapping(deleteMappings));
    }

    /**
     * Helper method for checking if a custom mapping exists
     *
     * @param mappings The list of available mappings
     * @return true if the custom mapping exists in the list of available mappings, false otherwise
     * @throws JSONException
     */
    private boolean existsMapping(JSONArray mappings) throws JSONException
    {
        boolean result = false;
        for (int i = 0; i < mappings.length(); i++)
        {
            String from = mappings.getJSONObject(i).getString("from");
            String to = mappings.getJSONObject(i).getString("to");
            if (from.equalsIgnoreCase("messageTo") && to.equalsIgnoreCase("rmc:Wibble"))
            {
                result = true;
                break;
            }
        }
        return result;
    }
}
