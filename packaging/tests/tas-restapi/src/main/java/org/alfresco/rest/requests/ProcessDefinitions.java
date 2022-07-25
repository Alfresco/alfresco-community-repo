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
import org.alfresco.rest.model.RestFormModelsCollection;
import org.alfresco.rest.model.RestHtmlResponse;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.rest.model.RestProcessDefinitionModelsCollection;
import org.alfresco.utility.Utility;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /process-definitions path
 *
 */
public class ProcessDefinitions extends ModelRequest<ProcessDefinitions>
{
    RestProcessDefinitionModel processDefinition;

    public ProcessDefinitions(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public ProcessDefinitions(RestProcessDefinitionModel processDefinition, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.processDefinition = processDefinition;
    }

    /**
     * Retrieve 100 process definitions (this is the default size when maxItems is not specified) from Alfresco using GET call on "/process-definitions"
     *
     * @return
     * @throws JsonToModelConversionException
     */
    public RestProcessDefinitionModelsCollection getAllProcessDefinitions()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "process-definitions?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestProcessDefinitionModelsCollection.class, request);
    }

    /**
     * Retrieves a process definition using GET call on "/process-definitions/{processDefinitionId}"
     *
     * @return
     */
    public RestProcessDefinitionModel getProcessDefinition()
    {
        Utility.checkObjectIsInitialized(processDefinition, "processDefinition");
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "process-definitions/{processDefinitionId}?{parameters}",
                processDefinition.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestProcessDefinitionModel.class, request);
    }

    /**
     * Retrieves an image that represents a single process definition using GET call on "/process-definitions/{processDefinitionId}/image"
     *
     * @return
     */
    public RestHtmlResponse getProcessDefinitionImage()
    {
        Utility.checkObjectIsInitialized(processDefinition, "processDefinition");
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "process-definitions/{processDefinitionId}/image", processDefinition.getId());
        return restWrapper.processHtmlResponse(request);
    }

    /**
     * Retrieves start form type definitions using GET call on "/process-definitions/{processDefinitionId}/start-form-model"
     *
     * @return
     */
    public RestFormModelsCollection getProcessDefinitionStartFormModel()
    {
        Utility.checkObjectIsInitialized(processDefinition, "processDefinition.onModel()");
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "process-definitions/{processDefinitionId}/start-form-model?{parameters}",
                processDefinition.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestFormModelsCollection.class, request);
    }
}
