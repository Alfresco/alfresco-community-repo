/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.requests;

import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.util.Map;

import org.alfresco.rest.core.RestAPI;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.FileplanComponentTypes;
import org.alfresco.rest.model.RestFilePlanComponentModel;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * ReST wrapper for IG APIs
 *
 * @author Tuna Aksoy
 * @author kconkas
 * @since 1.0
 */
@Component
@Scope(value = "prototype")
public class RestFilePlanComponentApi extends RestAPI
{
    
    /**
     * Get file plan component
     * @param filePlanComponentId
     * @return {@link RestFilePlanComponentModel} for filePlanComponentId
     * @throws Exception for non-existent components
     */
    public RestFilePlanComponentModel getFilePlanComponent(String filePlanComponentId) throws Exception
    {
        RestRequest request = simpleRequest(GET, "fileplan-components/{fileplanComponentId}", filePlanComponentId);
        return usingRestWrapper().processModel(RestFilePlanComponentModel.class, request);
    }
    
    /**
     * Create child file plan component
     * @param parentFilePlanComponentId
     * @param componentName
     * @param type
     * @param properties
     * @return
     * @throws Exception
     */
    public RestFilePlanComponentModel createFilePlanComponent(String parentFilePlanComponentId, String componentName, 
        FileplanComponentTypes componentType, Map<String, String> properties) throws Exception
    {
        if (componentName == null)
            throw new IllegalArgumentException("Child component name missing");
        
        JSONObject body = new JSONObject();
        body.put("name", componentName);
        body.put("nodeType", componentType.toString());
        if (properties != null)
        {
            body.put("properties", properties);
        }
        
        RestRequest request = requestWithBody(POST, body.toString(), "fileplan-components/{fileplanComponentId}/children", parentFilePlanComponentId);
        return usingRestWrapper().processModel(RestFilePlanComponentModel.class, request);
    }
}
