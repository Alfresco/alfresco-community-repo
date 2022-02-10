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
package org.alfresco.rest.v0;

import static org.testng.AssertJUnit.assertTrue;

import java.text.MessageFormat;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.custom.CustomDefinitions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Methods to make API requests using v0 API on Records Management Custom Model Reference Definitions
 *
 * @author Rodica Sutu
 * @since 2.6
 */
@Component
public class CustomDefinitionsAPI extends BaseAPI
{
    /**
     * custom references endpoint
     */
    private static final String CUSTOM_REFERENCE_API_ENDPOINT = "{0}rma/admin/customreferencedefinitions";

    /**
     * create reference endpoint
     */
    private static final String CREATE_RELATIONSHIP_API_ENDPOINT = "{0}node/{1}/customreferences";

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDefinitionsAPI.class);

    /**
     * Helper method to get the reference id for a custom reference
     *
     * @param adminUser        user with administrative privileges
     * @param adminPassword    password for adminUser
     * @param customDefinition custom reference definition name
     * @return  <code>reference id</code>   if the customDefinition is found
     *          <code> null </code> otherwise
     *
     */
    public String getCustomReferenceId(String adminUser, String adminPassword, String customDefinition)
    {

        JSONObject getResponse = doGetRequest(adminUser, adminPassword, CUSTOM_REFERENCE_API_ENDPOINT);
        if (getResponse != null)
        {
            try
            {
                JSONArray customDefinitions = getResponse.getJSONObject("data").getJSONArray("customReferences");
                for (int i = 0; i < customDefinitions.length(); i++)
                {
                    JSONObject item = customDefinitions.getJSONObject(i);
                    boolean hasSource = customDefinition.equalsIgnoreCase(
                            item.has("source") ? item.getString("source") : null
                                                                         );

                    boolean hasTarget = customDefinition.equalsIgnoreCase(
                            item.has("target") ? item.getString("target") : null
                                                                         );

                    boolean hasLabel = customDefinition.equalsIgnoreCase(
                            item.has("label") ? item.getString("label") : null
                                                                        );
                    if ( hasSource || hasTarget || hasLabel)
                    {
                        return item.getString("refId");
                    }
                }

            }
            catch (JSONException error)
            {
                LOGGER.error("Unable to get the refId for the custom reference definition {}", customDefinition);
            }
        }
        return null;
    }

    /**
     * Helper method to add custom reference instance to the specified record node
     *
     * @param adminUser     user with administrative privileges
     * @param adminPassword password for adminUser
     * @param recordNodeIdFrom node ref to set a custom reference
     * @param recordNodeIdTo        node ref of the to record
     * @param   relationshipType    relation type to be created
     * @throws AssertionError if the creation fails.
     */
    public void createRelationship(
            String adminUser,
            String adminPassword,
            String recordNodeIdFrom,
            String recordNodeIdTo,
            CustomDefinitions relationshipType)
    {
       //create the request body
        JSONObject requestParams = new JSONObject();
        requestParams.put("toNode", NODE_REF_WORKSPACE_SPACES_STORE + recordNodeIdTo);
        requestParams.put("refId", getCustomReferenceId(adminUser, adminPassword, relationshipType
                .getDefinition()));
        //send the API request to create the relationship
        JSONObject setRelationshipStatus = doPostRequest(adminUser, adminPassword, requestParams,
                MessageFormat.format(CREATE_RELATIONSHIP_API_ENDPOINT, "{0}", NODE_PREFIX + recordNodeIdFrom));
        //check the response
        boolean success = (setRelationshipStatus != null) && setRelationshipStatus.getBoolean("success");
        assertTrue("Creating relationship from " + recordNodeIdFrom + " to " + recordNodeIdTo + " failed.", success);
    }

}
