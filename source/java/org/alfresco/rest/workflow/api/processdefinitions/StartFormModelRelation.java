/* Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
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
