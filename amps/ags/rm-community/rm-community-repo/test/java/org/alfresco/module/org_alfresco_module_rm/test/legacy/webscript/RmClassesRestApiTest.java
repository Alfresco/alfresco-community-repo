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
 * REST API Tests for Class Definitions
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RmClassesRestApiTest extends BaseRMWebScriptTestCase
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
    private static final String RM_ASPECTS_URL = "/api/rm/classes?cf=%s&siteId=%s";

    /**
     * Test the REST API to retrieve the list of rm aspects
     *
     * @throws IOException
     * @throws JSONException
     */
    public void testRmGetAspectDefinitions() throws IOException, JSONException
    {
        // Format url and send request
        String url = String.format(RM_ASPECTS_URL, "aspect", siteId);
        Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        // Check the content from the response
        String contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json array
        JSONArray contentAsJson = new JSONArray(contentAsString);
        assertNotNull(contentAsJson);

        // Get a (sub)list of available dm/rm aspects
        List<String> dmAspects = getDmAspects();
        List<String> rmAspects = getRmAspects();

        // Get the list of rm aspects from the response and check it
        List<String> rmAspectList = new ArrayList<>();
        for (int i = 0; i < contentAsJson.length(); i++)
        {
            String name = contentAsJson.getJSONObject(i).getString("name");
            assertNotNull(name);
            rmAspectList.add(name);
            assertFalse(dmAspects.contains(name));
        }
        assertTrue(rmAspectList.containsAll(rmAspects));

        // Get the list of dm aspects and check them. It also contains rm related aspects.

        // Format url and send request
        url = String.format(RM_ASPECTS_URL, "aspect", collabSiteId);
        response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        // Check the content from the response
        contentAsString = response.getContentAsString();
        assertNotNull(contentAsString);

        // Convert the response to json array
        contentAsJson = new JSONArray(contentAsString);
        assertNotNull(contentAsJson);

        // Get the list of dm aspects from the response and check it
        List<String> dmAspectList = new ArrayList<>();
        for (int i = 0; i < contentAsJson.length(); i++)
        {
            String name = contentAsJson.getJSONObject(i).getString("name");
            assertNotNull(name);
            dmAspectList.add(name);
        }
        assertTrue(dmAspectList.containsAll(dmAspects));
        // the list of dm aspescts contains also rm aspects
        assertTrue(dmAspectList.containsAll(rmAspects));
    }

    /**
     * Returns a (sub)list of rm aspects
     *
     * @return A (sub)list of rm aspects
     */
    private List<String> getRmAspects()
    {
        return Arrays.asList(new String[]
        {
            "rma:ascended",
            "rma:recordMetaData",
            "rma:vitalRecordDefinition"
        });
    }

    /**
     * Returns a (sub)list of dm aspects
     *
     * @return A (sub)list of dm aspects
     */
    private List<String> getDmAspects()
    {
        return Arrays.asList(new String[]
        {
            "emailserver:attached",
            "bpm:assignees",
            "cm:likesRatingSchemeRollups",
            "wf:parallelReviewStats",
            "sys:localized"
        });
    }
}
