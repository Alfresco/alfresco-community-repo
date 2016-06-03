package org.alfresco.rest.workflow.api.processdefinitions;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.ProcessDefinitions;
import org.alfresco.rest.workflow.api.model.FormModelElement;

/**
 * 
 * @author Tijs Rademakers
 *
 */
@RelationshipResource(name = "start-form-model", entityResource = ProcessDefinitionsRestEntityResource.class, title = "Start form model of a process definition")
public class StartFormModelRelation implements RelationshipResourceAction.Read<FormModelElement>
{
    private ProcessDefinitions processDefinitions;

	public void setProcessDefinitions(ProcessDefinitions processDefinitions)
	{
		this.processDefinitions = processDefinitions;
	}

    /**
     * List the tasks candidate users and groups.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Get Start Form Model of a Process Definition", description = "Get a paged list of the start form model of a process definition")
    public CollectionWithPagingInfo<FormModelElement> readAll(String definitionId, Parameters parameters)
    {
        return processDefinitions.getStartFormModel(definitionId, parameters.getPaging());
    }
}
