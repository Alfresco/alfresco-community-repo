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
import java.util.Map;

import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.FieldProcessorRegistry;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.forms.processor.node.ItemData;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Smith
 */
public class TaskFormProcessor extends ContentModelFormProcessor<WorkflowTask, WorkflowTask>
{
    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(TaskFormProcessor.class);
    
    private TypedPropertyValueGetter valueGetter;
    private DataKeyMatcher keyMatcher;
    private WorkflowService workflowService;

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
        this.keyMatcher = new DataKeyMatcher(namespaceService);
        this.valueGetter = new TypedPropertyValueGetter(dictionaryService);
    }

    @Override
    protected WorkflowTask getTypedItem(Item item)
    {
        ParameterCheck.mandatory("item", item);
        String id = item.getId();
        WorkflowTask task = workflowService.getTaskById(id);
        return task;
    }

    @Override
    protected WorkflowTask internalPersist(WorkflowTask task, FormData data)
    {
        TaskUpdater updater = new TaskUpdater(task.id, workflowService);
        ItemData<WorkflowTask> itemData = makeItemData(task);
        for (FieldData fieldData : data) 
        {
            addFieldToSerialize(updater, itemData, fieldData);
        }
        return updater.update();
    }

    private void addFieldToSerialize(TaskUpdater updater,
            ItemData<?> itemData,
            FieldData fieldData)
    {
        String name = fieldData.getName();
        DataKeyInfo keyInfo = keyMatcher.match(name);
        if (keyInfo == null || 
                    FieldType.TRANSIENT_PROPERTY == keyInfo.getFieldType() )
        {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Ignoring unrecognized field: " + name);
            return;
        }
        
        QName fullName = keyInfo.getQName();
        Object rawValue = fieldData.getValue();
        if (FieldType.PROPERTY == keyInfo.getFieldType()) 
        {
            Serializable propValue = getPropertyValueToPersist(fullName, rawValue, itemData);
            // TODO What if the user wants to set prop to null?
            if (propValue != null)
            {
                updater.addProperty(fullName, propValue);
            }
        }
        else if (FieldType.ASSOCIATION == keyInfo.getFieldType())
        {
            if (rawValue instanceof String)
            {
                updater.changeAssociation(fullName, (String) rawValue, keyInfo.isAdd());
            }
        }
    }

    private Serializable getPropertyValueToPersist(QName fullName,
            Object value,
            ItemData<?> itemData)
    {
        PropertyDefinition propDef = itemData.getPropertyDefinition(fullName);
        if (propDef == null)
        {
            propDef = dictionaryService.getProperty(fullName);
        }
        if (propDef != null)
        {
            return valueGetter.getValue(value, propDef);
        }
        return (Serializable) value;
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
        return null;
    }

    /**
     * Sets the Workflow Service.
     * 
     * @param workflowService
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.alfresco.repo.forms.processor.task.ContentModelFormProcessor#
     * setNamespaceService(org.alfresco.service.namespace.NamespaceService)
     */
    @Override
    public void setNamespaceService(NamespaceService namespaceService)
    {
        super.setNamespaceService(namespaceService);
        this.keyMatcher = new DataKeyMatcher(namespaceService);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#setDictionaryService(org.alfresco.service.cmr.dictionary.DictionaryService)
     */
    @Override
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        super.setDictionaryService(dictionaryService);
        this.valueGetter = new TypedPropertyValueGetter(dictionaryService);
    }
}
