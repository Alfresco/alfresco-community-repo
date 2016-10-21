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

import static org.alfresco.rest.body.IgJsonBodyGenerator.filePlanComponentCreate;
import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.alfresco.rest.core.RestAPI;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestFilePlanComponentModel;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * REST wrapper for IG APIs
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
     * Create file plan component
     * @param model
     * @return
     * @throws Exception
     */
    public RestFilePlanComponentModel createFilePlanComponent(RestFilePlanComponentModel model) throws Exception
    {
        RestRequest request = requestWithBody(POST, 
            filePlanComponentCreate(model).toString(), 
            "fileplan-components/{fileplanComponentId}/children", 
            model.getId());
        return usingRestWrapper().processModel(RestFilePlanComponentModel.class, request);
    }

    /**
     * Update file plan component
     * @param filePlanComponentId
     * @param requestBody update body, refer to API reference for details
     * @return {@link RestFilePlanComponentModel} for <code>filePlanComponentId</code>
     * @throws Exception
     */
    public RestFilePlanComponentModel updateFilePlanComponent(String filePlanComponentId, RestFilePlanComponentModel update) throws Exception
    {
        RestRequest request = requestWithBody(PUT, 
            filePlanComponentCreate(update).toString(), 
            "fileplan-components/{fileplanComponentId}", 
            filePlanComponentId);
        return usingRestWrapper().processModel(RestFilePlanComponentModel.class, request);
    }
    
    /**
     * Delete file plan component
     * @param filePlanComponentId
     * @param deletePermanently if set to <code>true</code> delete without moving to the trashcan
     * @throws Exception
     */
    public RestRequest deleteFilePlanComponent(String filePlanComponentId, boolean deletePermanently) throws Exception
    {
        JSONObject body = new JSONObject();
        if (deletePermanently) body.put("permanent", deletePermanently);

        RestRequest request = requestWithBody(DELETE, body.toString(), "fileplan-components/{fileplanComponentId}", filePlanComponentId);
        usingRestWrapper().processEmptyModel(request);
        return request;
    }
}
