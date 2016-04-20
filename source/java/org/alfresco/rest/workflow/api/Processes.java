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
