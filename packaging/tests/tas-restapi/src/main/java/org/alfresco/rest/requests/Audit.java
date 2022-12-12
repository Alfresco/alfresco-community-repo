/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.requests;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestAuditAppModelsCollection;
import org.alfresco.rest.model.RestAuditEntryModel;
import org.alfresco.rest.model.RestAuditAppModel;
import org.alfresco.rest.model.RestAuditEntryModelsCollection;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /audit-applications path
 *
 */
public class Audit extends ModelRequest<Audit>
{

    public Audit(RestWrapper restWrapper)
    {
      super(restWrapper);
    }
    

    /**
     * Gets a list of audit applications in this repository using GET call on "/audit-applications"
     * 
     * @return
     * @throws JsonToModelConversionException
     */
    public RestAuditAppModelsCollection getAuditApplications() throws JsonToModelConversionException
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "audit-applications?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestAuditAppModelsCollection.class, request);
    }

    /**
     * Retrieves an audit application info with ID using GET call on "/audit-applications/{auditApplicationId}"
     * 
     * @param auditApplicationId
     * @return
     */
    public RestAuditAppModel getAuditApp(RestAuditAppModel restAuditAppModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "audit-applications/{auditApplicationId}?{parameters}", restAuditAppModel.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestAuditAppModel.class, request);
    }

    /**
     * Retrieves a list of audit entries for audit application auditApplicationId using GET call on "/audit-applications/{auditApplicationId}/audit-entries"
     * 
     * @param auditApplicationId
     * @return
     */
    public RestAuditEntryModelsCollection listAuditEntriesForAnAuditApplication(String auditApplicationId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "audit-applications/{auditApplicationId}/audit-entries?{parameters}", auditApplicationId, restWrapper.getParameters());
        return restWrapper.processModels(RestAuditEntryModelsCollection.class, request);
    }

    /**
     * Disable or re-enable the audit application auditApplicationId using PUT call on "/audit-applications/{auditApplicationId}"
     * 
     * @param restAuditAppModel
     * @param key
     * @param value
     * @return
     */
    public RestAuditAppModel updateAuditApp(RestAuditAppModel restAuditAppModel, String key, String value)
    {
        String postBody = JsonBodyGenerator.keyValueJson(key, value);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "audit-applications/{auditApplicationId}",restAuditAppModel.getId());
        return restWrapper.processModel(RestAuditAppModel.class, request);
    }

    /**
     * Retrieves an audit entry auditEntryId for audit application auditApplicationId using GET call on "/audit-applications/{auditApplicationId}/audit-entries/{auditEntryId}"
     * 
     * @param auditApplicationId
     * @param auditEntryId
     * @return
     */
    public RestAuditEntryModel getAuditEntryForAnAuditApplication(String auditApplicationId, String auditEntryId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "audit-applications/{auditApplicationId}/audit-entries/{auditEntryId}?{parameters}", auditApplicationId, auditEntryId, restWrapper.getParameters());
        return restWrapper.processModel(RestAuditEntryModel.class, request);
    }

    /**
     * Deletes an audit entry auditEntryId for audit application auditApplicationId using DELETE call on "/audit-applications/{auditApplicationId}/audit-entries/{auditEntryId}"
     * 
     * @param auditApplicationId
     * @param auditEntryId
     * @return
     */
    public void deleteAuditEntryForAnAuditApplication(String auditApplicationId, String auditEntryId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "audit-applications/{auditApplicationId}/audit-entries/{auditEntryId}", auditApplicationId, auditEntryId);
        restWrapper.processEmptyModel(request);
    }

    /**
     * Deletes audit entries for audit application auditApplicationId using DELETE call on "/audit-applications/{auditApplicationId}/audit-entries"
     * 
     * @param auditApplicationId
     * @return
     */
    public void deleteAuditEntriesForAnAuditApplication(String auditApplicationId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "audit-applications/{auditApplicationId}/audit-entries?{parameters}", auditApplicationId, restWrapper.getParameters());
        restWrapper.processEmptyModel(request);
    }

    /** 
     * Retrieves a list of audit entries for a node nodeId using GET call on "/nodes/{nodeId}/audit-entries"
     * 
     * @param nodeId
     * @return
     */
    public RestAuditEntryModelsCollection listAuditEntriesForNode(String nodeId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/audit-entries?{parameters}", nodeId, restWrapper.getParameters());
        return restWrapper.processModels(RestAuditEntryModelsCollection.class, request);
    }

}
