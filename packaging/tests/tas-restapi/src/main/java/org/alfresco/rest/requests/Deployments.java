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

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestDeploymentModel;
import org.alfresco.rest.model.RestDeploymentModelsCollection;
import org.springframework.http.HttpMethod;

/**
 * Created by Claudia Agache on 10/4/2016.
 */
public class Deployments extends ModelRequest<Deployments>
{
    RestDeploymentModel deployment;

    public Deployments(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public Deployments(RestDeploymentModel deployment, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.deployment = deployment;
    }

    /**
     * Retrieve 100 deployments (this is the default size when maxItems is not specified) from Alfresco using GET call on "/deployments"
     *
     * @return
     * @throws JsonToModelConversionException
     */
    public RestDeploymentModelsCollection getDeployments()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deployments?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestDeploymentModelsCollection.class, request);
    }
    
    /**
     * Delete the specified deployment from Alfresco using DELETE call on "/deployments/{deploymentId}"
     *
     * @return
     * @throws JsonToModelConversionException
     */
    public void deleteDeployment()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "deployments/{deploymentId}", deployment.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Retrieve the specified deployment from Alfresco using GET call on "/deployments/{deploymentId}"
     *
     * @return
     * @throws JsonToModelConversionException
     */
    public RestDeploymentModel getDeployment()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deployments/{deploymentId}?{parameters}",
                deployment.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestDeploymentModel.class, request);
    }

}
