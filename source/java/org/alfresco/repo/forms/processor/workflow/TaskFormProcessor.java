/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.repo.forms.processor.FieldProcessorRegistry;
import org.alfresco.repo.forms.processor.node.ItemData;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Smith
 */
public class TaskFormProcessor extends AbstractWorkflowFormProcessor<WorkflowTask, WorkflowTask>
{
    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(TaskFormProcessor.class);

    // Constructor for Spring
    public TaskFormProcessor()
    {
        super();
    }

    // Constructor for tests.
    public TaskFormProcessor(WorkflowService workflowService, NamespaceService namespaceService,
            DictionaryService dictionaryService, FieldProcessorRegistry fieldProcessorRegistry)
    {
        this.workflowService = workflowService;
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;
        this.fieldProcessorRegistry = fieldProcessorRegistry;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.AbstractWorkflowFormProcessor#getTypedItemForDecodedId(java.lang.String)
     */
    @Override
    protected WorkflowTask getTypedItemForDecodedId(String itemId)
    {
        return workflowService.getTaskById(itemId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemType(java
     * .lang.Object)
     */
    @Override
    protected String getItemType(WorkflowTask item)
    {
        TypeDefinition typeDef = item.definition.metadata;
        return typeDef.getName().toPrefixString(namespaceService);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemURI(java
     * .lang.Object)
     */
    @Override
    protected String getItemURI(WorkflowTask item)
    {
        // TODO Check this URL is OK.
        return "/api/task-instances/" + item.id;
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.task.ContentModelFormProcessor#getLogger
     * ()
     */
    @Override
    protected Log getLogger()
    {
        return LOGGER;
    }

    @Override
    protected TypeDefinition getBaseType(WorkflowTask task)
    {
        return task.definition.metadata;
    }

    @Override
    protected Map<QName, Serializable> getPropertyValues(WorkflowTask task)
    {
        return task.properties;
    }

    @Override
    protected Map<QName, Serializable> getAssociationValues(WorkflowTask item)
    {
        return item.properties;
    }

    @Override
    protected Map<String, Object> getTransientValues(WorkflowTask item)
    {
        StringBuilder builder = new StringBuilder();
        WorkflowTransition[] transitions = item.definition.node.transitions;
        if(transitions == null)
            return Collections.<String, Object>singletonMap(TransitionFieldProcessor.KEY, "");
        for (WorkflowTransition transition : transitions)
        {
            builder.append(transition.getId());
            builder.append("|");
            builder.append(transition.getTitle());
            builder.append(",");
        }
        builder.deleteCharAt(builder.length()-1);
        String values = builder.toString();
        return Collections.<String, Object>singletonMap(TransitionFieldProcessor.KEY, values);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.AbstractWorkflowFormProcessor#makeFormPersister(java.lang.Object)
     */
    @Override
    protected ContentModelFormPersister<WorkflowTask> makeFormPersister(WorkflowTask item)
    {
        ItemData<WorkflowTask> itemData = makeItemData(item);
        return new TaskFormPersister(itemData, namespaceService, dictionaryService, workflowService, nodeService, LOGGER);
    }
}
