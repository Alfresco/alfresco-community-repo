package org.alfresco.rest.workflow.api.processdefinitions;

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.ProcessDefinitions;
import org.alfresco.rest.workflow.api.model.ProcessDefinition;

@EntityResource(name="process-definitions", title = "Process definitions")
public class ProcessDefinitionsRestEntityResource implements EntityResourceAction.Read<ProcessDefinition>, 
                                                             EntityResourceAction.ReadById<ProcessDefinition>,
                                                             BinaryResourceAction.Read {

    ProcessDefinitions processDefinitions;
    
    public void setProcessDefinitions(ProcessDefinitions processDefinitions)
    {
        this.processDefinitions = processDefinitions;
    }

    @Override
    @WebApiDescription(title = "Get Process Definitions", description = "Get information for the process definitions")
    @WebApiParameters(value = { 
            @WebApiParam(name = "key", title = "Only return process definitions with the given key", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "category", title = "Only return process definitions with the given category", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "name", title = "Only return process definitions with the given name", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "deploymentId", title = "Only return process definitions with the given definitionId", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "version", title = "Only return process definitions with the given version", kind=ResourceParameter.KIND.QUERY_STRING)})
    public CollectionWithPagingInfo<ProcessDefinition> readAll(Parameters params)
    {
        return processDefinitions.getProcessDefinitions(params);
    }

    @Override
    public ProcessDefinition readById(String id, Parameters parameters) throws EntityNotFoundException
    {
        return processDefinitions.getProcessDefinition(id);
    }
    
    @Override
    @WebApiDescription(title = "Get a process definition image", description = "Get a process definition image")
    @BinaryProperties({"image"})
    public BinaryResource readProperty(String id, Parameters parameters) throws EntityNotFoundException
    {
        return processDefinitions.getProcessDefinitionImage(id);
    }
}
