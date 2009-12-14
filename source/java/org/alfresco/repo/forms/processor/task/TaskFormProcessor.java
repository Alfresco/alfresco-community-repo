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

package org.alfresco.repo.forms.processor.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.FilteredFormProcessor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * @author Nick Smith
 */
public class TaskFormProcessor extends FilteredFormProcessor<WorkflowTask, WorkflowTask>
{
    /** Logger */
    // private static final Log logger =
    // LogFactory.getLog(TaskFormProcessor.class);

    private final WorkflowService workflowService;

    private final NamespaceService namespaceService;

    private final DictionaryService dictionaryService;

    private final FieldDefinitionFactory factory;

    public TaskFormProcessor(WorkflowService workflowService, NamespaceService namespaceService,
                DictionaryService dictionaryService)
    {
        this.workflowService = workflowService;
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;
        this.factory = new FieldDefinitionFactory(namespaceService);
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#getTypedItem(
     * org.alfresco.repo.forms.Item)
     */
    @Override
    protected WorkflowTask getTypedItem(Item item)
    {
        ParameterCheck.mandatory("item", item);
        String id = item.getId();
        WorkflowTask task = workflowService.getTaskById(id);
        return task;
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#internalGenerate
     * (java.lang.Object, java.util.List, java.util.List,
     * org.alfresco.repo.forms.Form, java.util.Map)
     */
    @Override
    protected void internalGenerate(WorkflowTask item, List<String> fields, List<String> forcedFields, Form form,
                Map<String, Object> context)
    {
        TypeDefinition typeDef = item.definition.metadata;
        String type = typeDef.getName().toPrefixString(namespaceService);
        setFormItemType(form, type);

        // TODO Check this URL is OK.
        setFormItemUrl(form, "/api/task/" + item.id);

        FieldCreationData data = new FieldCreationData(item, forcedFields, null);
        List<FieldInfo> fieldsToAdd = generateFields(data, fields);
        for (FieldInfo fieldToAdd : fieldsToAdd)
        {
            if (fieldToAdd.isValid())
            {
                FieldDefinition fieldDef = fieldToAdd.getFieldDefinition();
                form.addFieldDefinition(fieldDef);
                Object value = data.getPropValues().get(fieldToAdd.getFullName());
                if (value != null)
                {
                    form.addData(fieldDef.getDataKeyName(), value);
                }
            }
        }
    }

    private List<FieldInfo> generateFields(FieldCreationData data, List<String> fields)
    {
        ArrayList<FieldInfo> fieldData = new ArrayList<FieldInfo>(fields.size());
        for (String fieldName : fields)
        {
            fieldData.add(makeFieldInfo(data, fieldName));
        }
        return fieldData;
    }

    private FieldInfo makeFieldInfo(FieldCreationData data, String fieldName)
    {
        return new FieldInfo(fieldName, data, factory, dictionaryService, namespaceService);
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#internalPersist
     * (java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected WorkflowTask internalPersist(WorkflowTask item, FormData data)
    {
        //TODO Implement this method properly.
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        for (FieldData fieldData : data)
        {
            fieldData.getName();
        }
        workflowService.updateTask(item.id, props, null, null);
        return null;
    }

}
