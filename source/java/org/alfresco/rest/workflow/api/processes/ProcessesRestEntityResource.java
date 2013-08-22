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

import java.util.ArrayList;
import java.util.List;

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
import org.alfresco.rest.workflow.api.Processes;
import org.alfresco.rest.workflow.api.model.ProcessInfo;

@EntityResource(name="processes", title = "Processes")
public class ProcessesRestEntityResource implements EntityResourceAction.Read<ProcessInfo>, 
                                                           EntityResourceAction.ReadById<ProcessInfo>,
                                                           EntityResourceAction.Create<ProcessInfo>,
                                                           EntityResourceAction.Delete,
                                                           BinaryResourceAction.Read {

    Processes processes;
    
    public void setProcesses(Processes processes)
    {
        this.processes = processes;
    }

    @Override
    @WebApiDescription(title = "Start a new process instance", description = "Start a new process instance")
    @WebApiParameters(value = { 
            @WebApiParam(name = "processDefinitionId", title = "The process instance will be started in the given process definition", kind=ResourceParameter.KIND.HTTP_BODY_OBJECT),
            @WebApiParam(name = "processDefinitionKey", title = "The process instance will be started in the latest version of the process definition having the given key", kind=ResourceParameter.KIND.HTTP_BODY_OBJECT),
            @WebApiParam(name = "businessKey", title = "The business key is a unique, user defined reference to the newly created process instance that can be used in future queries", kind=ResourceParameter.KIND.HTTP_BODY_OBJECT),
            @WebApiParam(name = "variables", title = "A set of process instance variables that will be set on the new process instance", kind=ResourceParameter.KIND.HTTP_BODY_OBJECT),
            @WebApiParam(name = "items", title = "A list of nodes in the repository that will be associated with this process instance", kind=ResourceParameter.KIND.HTTP_BODY_OBJECT),
            })
    public List<ProcessInfo> create(List<ProcessInfo> entity, Parameters parameters)
    {
        List<ProcessInfo> result = new ArrayList<ProcessInfo>(entity.size());
        for (ProcessInfo process : entity)
        {
           result.add(processes.create(process));
        }
        return result;
    }

    @Override
    @WebApiDescription(title = "Get Process Instances", description = "Get information for the process instances")
    @WebApiParameters(value = { @WebApiParam(name = "where", title = "Where parameter to define the process query", kind=ResourceParameter.KIND.QUERY_STRING)})
    public CollectionWithPagingInfo<ProcessInfo> readAll(Parameters params)
    {
        return processes.getProcesses(params);
    }
    
    @Override
    @WebApiDescription(title = "Get a process instance image", description = "Get a process instance image")
    @BinaryProperties({"image"})
    public BinaryResource readProperty(String entityId, Parameters parameters) throws EntityNotFoundException
    {
        return processes.getProcessImage(entityId);
    }

    @Override
    public ProcessInfo readById(String id, Parameters parameters) throws EntityNotFoundException
    {
        return processes.getProcess(id);
    }

    @Override
    public void delete(String id, Parameters parameters)
    {
        processes.deleteProcess(id);
    }
}
