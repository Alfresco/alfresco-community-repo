/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.forms;

import java.io.IOException;
import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class FormRestApiJsonPost_Test extends AbstractTestFormRestApi
{
    private static final String PROP_CM_DESCRIPTION = "prop_cm_description";
    private static final String APPLICATION_JSON = "application/json";

    public void testSimpleJsonPostRequest() throws IOException, JSONException
    {
        // Retrieve and store the original property value.
        Serializable originalDescription =
            nodeService.getProperty(testNodeRef, ContentModel.PROP_DESCRIPTION);
        assertEquals(TEST_FORM_DESCRIPTION, originalDescription);
        
        // Construct some JSON to represent a new value.
        JSONObject jsonPostData = new JSONObject();
        final String proposedNewDescription = "Modified Description";
        jsonPostData.put(PROP_CM_DESCRIPTION, proposedNewDescription);
        
        // Submit the JSON request.
        Response ignoredRsp = sendRequest(new PostRequest(testNodeUrl, jsonPostData.toString(),
                APPLICATION_JSON), 200);

        // The nodeService should give us the modified property.
        Serializable modifiedDescription =
            nodeService.getProperty(testNodeRef, ContentModel.PROP_DESCRIPTION);
        assertEquals(proposedNewDescription, modifiedDescription);

        // The Rest API should also give us the modified property.
        Response response = sendRequest(new GetRequest(testNodeUrl), 200);
        JSONObject jsonGetResponse = new JSONObject(response.getContentAsString());
        JSONObject jsonDataObj = (JSONObject)jsonGetResponse.get("data");
        assertNotNull(jsonDataObj);

        JSONObject formData = (JSONObject)jsonDataObj.get("formData");
        assertNotNull(formData);
        String retrievedValue = (String)formData.get(PROP_CM_DESCRIPTION);
        assertEquals(modifiedDescription, retrievedValue);
    }
}
