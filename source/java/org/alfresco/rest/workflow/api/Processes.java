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
package org.alfresco.rest.workflow.api;

import java.util.List;

import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.model.Item;
import org.alfresco.rest.workflow.api.model.ProcessInfo;
import org.alfresco.rest.workflow.api.model.Variable;

public interface Processes
{
    CollectionWithPagingInfo<ProcessInfo> getProcesses(Parameters parameters);
    
    ProcessInfo getProcess(String processId);
    
    ProcessInfo create(ProcessInfo process);
    
    CollectionWithPagingInfo<Item> getItems(String processId, Paging paging);
    
    Item getItem(String processId, String itemId);
    
    Item createItem(String processId, Item item);
    
    void deleteItem(String processId, String itemId);

    void deleteProcess(String id);

    CollectionWithPagingInfo<Variable> getVariables(String processId, Paging paging);

    Variable updateVariable(String processId, Variable entity);
    
    List<Variable> updateVariables(String processId, List<Variable> variables);

    void deleteVariable(String processId, String id);

    BinaryResource getProcessImage(String processId);
}
