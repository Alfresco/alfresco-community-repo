package org.alfresco.rest.workflow.api;

import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.model.FormModelElement;
import org.alfresco.rest.workflow.api.model.ProcessDefinition;

public interface ProcessDefinitions
{
    public CollectionWithPagingInfo<ProcessDefinition> getProcessDefinitions(Parameters parameters);
    
    public ProcessDefinition getProcessDefinition(String definitionId);
    
    public BinaryResource getProcessDefinitionImage(String definitionId);
    
    public CollectionWithPagingInfo<FormModelElement> getStartFormModel(String definitionId, Paging paging);
}
