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
