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
import org.alfresco.rest.model.RestCustomAspectModel;
import org.alfresco.rest.model.RestCustomModel;
import org.alfresco.rest.model.RestCustomTypeModel;
import org.alfresco.rest.model.RestGroupsModelsCollection;
import org.alfresco.utility.model.CustomAspectModel;
import org.alfresco.utility.model.CustomAspectPropertiesModel;
import org.alfresco.utility.model.CustomContentModel;
import org.springframework.http.HttpMethod;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

/**
 * @author Bogdan Bocancea
 */
public class CustomModelManager extends ModelRequest<CustomModelManager>
{
    private CustomContentModel customContentModel;

    public CustomModelManager(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public CustomModelManager(CustomContentModel customContentModel, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.customContentModel = customContentModel;
    }

    /**
     * Create a new custom model
     *
     * @param customContentModel
     * @return {@link RestCustomModel}
     */
    public RestCustomModel createCustomModel(CustomContentModel customContentModel)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, customContentModel.toJson(),
            "cmm?{parameters}", restWrapper.getParameters());
        return restWrapper.processModel(RestCustomModel.class, request);
    }

    /**
     * Retrieve one model using GET call on "cmm/{modelName}"
     * 
     * @return {@link RestCustomModel}
     */
    public RestCustomModel getModel()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "cmm/{modelName}?{parameters}", this.customContentModel.getName(),
                restWrapper.getParameters());
        return restWrapper.processModel(RestCustomModel.class, request);
    }
    
    public void activateModel()
    {
        String json = JsonBodyGenerator.keyValueJson("status", "ACTIVE");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "cmm/{modelName}?select=status", this.customContentModel.getName());
        restWrapper.processEmptyModel(request);
    }

    public void deactivateModel()
    {
        String json = JsonBodyGenerator.keyValueJson("status", "DRAFT");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "cmm/{modelName}?select=status", this.customContentModel.getName());
        restWrapper.processEmptyModel(request);
    }

    public void deleteModel()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "cmm/{modelName}", this.customContentModel.getName());
        restWrapper.processEmptyModel(request);
    }

    public RestCustomAspectModel createAspect(CustomAspectModel aspectModel)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, aspectModel.toJson(),
            "cmm/{modelName}/aspects?{parameters}", this.customContentModel.getName(), restWrapper.getParameters());
        return restWrapper.processModel(RestCustomAspectModel.class, request);
    }

    public RestCustomTypeModel createCustomType(RestCustomTypeModel customType)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, customType.toJson(),
                "cmm/{modelName}/types?{parameters}", this.customContentModel.getName(), restWrapper.getParameters());
        return restWrapper.processModel(RestCustomTypeModel.class, request);
    }
}
