/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.processes;

import java.util.List;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Processes;
import org.alfresco.rest.workflow.api.model.Variable;

/**
 * 
 * @author Tijs Rademakers
 *
 */
@RelationshipResource(name = "variables", entityResource = ProcessesRestEntityResource.class, title = "Variables for the current process")
public class ProcessVariablesRelation implements RelationshipResourceAction.Read<Variable>, RelationshipResourceAction.Create<Variable>, 
    RelationshipResourceAction.Update<Variable>, RelationshipResourceAction.Delete
{
    protected Processes processes;
    
    public void setProcesses(Processes processes)
    {
        this.processes = processes;
    }

    /**
     * List the variables.
     */
    @Override
    @WebApiDescription(title = "Get Task Variables", description = "Get a paged list of the task variables")
    public CollectionWithPagingInfo<Variable> readAll(String processId, Parameters parameters)
    {
        return processes.getVariables(processId, parameters.getPaging());
    }
    
    /**
     * Creates or updates multiple variables. If the variable name doesn't exist yet it will be created
     */
    @Override
    @WebApiDescription(title = "Create or Update Variables", description = "Create or update multiple variable")
    public List<Variable> create(String processId, List<Variable> variables, Parameters parameters)
    {
        return processes.updateVariables(processId, variables);
    }

    /**
     * Update a variable. If the variable name doesn't exist yet it will be created
     */
    @Override
    @WebApiDescription(title = "Update Variable", description = "Update a variable")
    public Variable update(String processId, Variable entity, Parameters parameters)
    {
        return processes.updateVariable(processId, entity);
    }

    /**
     * Delete a variable. If the variable name doesn't exist the delete call throws an exception.
     */
    @Override
    @WebApiDescription(title = "Delete Variable", description = "Delete a variable")
    public void delete(String processId, String id, Parameters parameters)
    {
        processes.deleteVariable(processId, id);
    }
}
