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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * REST API Tests for Properties Definitions
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RmPropertiesRestApiTest extends BaseRMWebScriptTestCase
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /** URL for the REST APIs */
    private static final String RM_TYPES_URL = "/api/rm/classes?cf=%s&siteId=%s";

    /**
     * Test the REST API to retrieve the list of rm types
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmGetTypeDefinitions() throws IOException, JSONException
    {
        // Format url and send request
        String url = String.format(RM_TYPES_URL, "type", siteId);
        Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        // Check the content from the response
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json array
        JSONArray contentAsJson = new JSONArray(contentAsString);
        assertNotNull(contentAsJson);

        // Get a (sub)list of available dm/rm types
        List<String> dmTypes = getDmTypes();
        List<String> rmTypes = getRmTypes();

        // Get the list of rm types from the response and check it
        List<String> rmTypeList = new ArrayList<>();
        for (int i = 0; i < contentAsJson.length(); i++)
        {
            String name = contentAsJson.getJSONObject(i).getString("name");
            assertNotNull(name);
            rmTypeList.add(name);
            assertFalse(dmTypes.contains(name));
        }
        assertTrue(rmTypeList.containsAll(rmTypes));

        // Get the list of dm types and check them. It also contains rm related types.

        // Format url and send request
        url = String.format(RM_TYPES_URL, "type", collabSiteId);
        response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        // Check the content from the response
        contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json array
        contentAsJson = new JSONArray(contentAsString);
        assertNotNull(contentAsJson);

        // Get the list of dm types from the response and check it
        List<String> dmTypeList = new ArrayList<>();
        for (int i = 0; i < contentAsJson.length(); i++)
        {
            String name = contentAsJson.getJSONObject(i).getString("name");
            assertNotNull(name);
            dmTypeList.add(name);
        }
        assertTrue(dmTypeList.containsAll(dmTypes));
        // the list of dm type contains also rm types
        assertTrue(dmTypeList.containsAll(rmTypes));
    }

    /**
     * Returns a (sub)list of rm types
     *
     * @return A (sub)list of rm types
     */
    private List<String> getRmTypes()
    {
        return Arrays.asList(new String[]
        {
            "rma:eventExecution",
            "rma:nonElectronicDocument",
            "rma:transfer"
        });
    }

    /**
     * Returns a (sub)list of dm types
     *
     * @return A (sub)list of dm types
     */
    private List<String> getDmTypes()
    {
        return Arrays.asList(new String[]
        {
            "cm:authority",
            "sys:descriptor",
            "app:folderlink",
            "wf:submitGroupReviewTask",
            "cmis:policy"
        });
    }
}
