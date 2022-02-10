/*-
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

package org.alfresco.rest.v0;

import java.io.IOException;
import java.text.MessageFormat;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.utility.model.UserModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The v0 API to get the node properties
 *
 * @since AGS 3.4
 */
@Component
public class NodePropertiesAPI extends BaseAPI
{
    /**
     * The URI for the get node API.
     */
    private static final String GET_NODE_API = "{0}alfresco/s/slingshot/node/{1}";

    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    /**
     * Get the node properties
     *
     * @param username
     * @param password
     * @param nodeId
     * @return JSONArray  object
     */
    protected JSONArray getNodeProperties(String username, String password, String nodeId)
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String requestURL = MessageFormat.format(GET_NODE_API, client.getAlfrescoUrl(), NODE_PREFIX + nodeId);

        // doRequest from BaseAPI cannot be used as  parsing the  response body to org.json.JSONObject is throwing an
        // JSONException
        // construct a get request
        HttpGet get = new HttpGet(requestURL);
        HttpResponse response = client.execute(username, password, get);
        HttpEntity entity = response.getEntity();
        String responseString;
        try
        {
            responseString = EntityUtils.toString(entity, "UTF-8");
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Failed to read the response", e);
        }
        client.close();
        Object obj = JSONValue.parse(responseString);
        JSONObject jsonObject = (JSONObject) obj;
        return (JSONArray) jsonObject.get("properties");
    }

    /**
     * Get the content url (bin name) for a node
     *
     * @param userModel
     * @param nodeId
     * @return Return the content url string
     */
    public String getContentUrl(UserModel userModel, String nodeId)
    {
        String contentProperty = getContentProperty(userModel, nodeId);
        if (contentProperty != null)
        {
            // get the first element before the first |
            // e.g.  "contentUrl=s3://-system-/fc077fe8-1742-4c45-a153-8309c857996b
            // .bin|mimetype=text/plain|size=19|encoding=ISO-8859-2|locale=en_US_|id=508"
            contentProperty = contentProperty.split("\\|")[0];
            return contentProperty.replaceAll("contentUrl=", "");
        }
        return null;
    }

    /**
     * Get the content property for a node
     *
     * @param userModel
     * @param nodeId
     * @return Return the content property string
     */
    public String getContentProperty(UserModel userModel, String nodeId)
    {
        JSONArray properties = getNodeProperties(userModel.getUsername(), userModel.getPassword(), nodeId);

        for (int i = 0; i < properties.size(); i++)
        {
            JSONObject object = (JSONObject) properties.get(i);
            JSONArray valuesArray = (JSONArray) object.get("values");
            if (valuesArray.toString().contains("contentUrl"))
            {
                return ((JSONObject) valuesArray.get(0)).get("value").toString();
            }
        }
        return null;
    }
}
