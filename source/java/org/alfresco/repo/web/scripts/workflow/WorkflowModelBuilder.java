/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 *
 */
public class WorkflowModelBuilder
{
    public static final String PERSON_LAST_NAME = "lastName";
    public static final String PERSON_FIRST_NAME = "firstName";
    public static final String PERSON_USER_NAME = "userName";

    public static final String TASK_PROPERTIES = "properties";
    public static final String TASK_OWNER = "owner";
    public static final String TASK_TYPE_DEFINITION_TITLE = "typeDefinitionTitle";
    public static final String TASK_STATE = "state";
    public static final String TASK_DESCRIPTION = "description";
    public static final String TASK_TITLE = "title";
    public static final String TASK_NAME = "name";
    public static final String TASK_URL = "url";
    public static final String TASK_IS_POOLED = "isPooled";
    public static final String TASK_ID = "id";
    
    public static final String WORKFLOW_DEFINITION_ID = "id";
    public static final String WORKFLOW_DEFINITION_URL = "url";
    public static final String WORKFLOW_DEFINITION_NAME = "name";
    public static final String WORKFLOW_DEFINITION_TITLE = "title";
    public static final String WORKFLOW_DEFINITION_DESCRIPTION = "description";
    
    private static final String PREFIX_SEPARATOR = Character.toString(QName.NAMESPACE_PREFIX);

    private final NamespaceService namespaceService;
    private final NodeService      nodeService;
    private final PersonService    personService;

    public WorkflowModelBuilder(NamespaceService namespaceService, NodeService nodeService, PersonService personService)
    {
        this.namespaceService = namespaceService;
        this.nodeService = nodeService;
        this.personService = personService;
    }

    /**
     * Returns a simple representation of a {@link WorkflowTask}.
     * @param task The task to be represented.
     * @param propertyFilters Specify which properties to include.
     * @return
     */
    public Map<String, Object> buildSimple(WorkflowTask task, Collection<String> propertyFilters)
    {
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put(TASK_ID, task.id);
        model.put(TASK_URL, getUrl(task));
        model.put(TASK_NAME, task.name);
        model.put(TASK_TITLE, task.title);
        model.put(TASK_DESCRIPTION, task.description);
        model.put(TASK_STATE, task.state.name());
        model.put(TASK_TYPE_DEFINITION_TITLE, task.definition.metadata.getTitle());

        model.put(TASK_IS_POOLED, isPooled(task.properties));
        Serializable owner = task.properties.get(ContentModel.PROP_OWNER);
        model.put(TASK_OWNER, getPersonModel((String) owner));
        
        model.put(TASK_PROPERTIES, buildProperties(task, propertyFilters));
        return model;
    }

    /**
     * Returns a simple representation of a {@link WorkflowDefinition}.
     * 
     * @param workflowDefinition the WorkflowDefinition object to be represented.
     * @return
     */
    public Map<String, Object> buildSimple(WorkflowDefinition workflowDefinition)
    {
        HashMap<String, Object> model = new HashMap<String, Object>();
        
        model.put(WORKFLOW_DEFINITION_ID, workflowDefinition.id);
        model.put(WORKFLOW_DEFINITION_URL, getUrl(workflowDefinition));
        model.put(WORKFLOW_DEFINITION_NAME, workflowDefinition.name);
        model.put(WORKFLOW_DEFINITION_TITLE, workflowDefinition.title);
        model.put(WORKFLOW_DEFINITION_DESCRIPTION, workflowDefinition.description);        
        
        return model;
    }

    private Object isPooled(Map<QName, Serializable> properties)
    {
        Collection<?> actors = (Collection<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
        return actors!=null && !actors.isEmpty();
    }

    private Map<String, Object> buildProperties(WorkflowTask task, Collection<String> propertyFilters)
    {
        Map<QName, Serializable> properties = task.properties;
        Collection<QName> keys;
        if (propertyFilters == null || propertyFilters.size() == 0)
        {
            Map<QName, PropertyDefinition> propDefs = task.definition.metadata.getProperties();
            Map<QName, AssociationDefinition> assocDefs = task.definition.metadata.getAssociations();
            Set<QName> propKeys = properties.keySet();
            keys = new HashSet<QName>(propDefs.size() + assocDefs.size() + propKeys.size());
            keys.addAll(propDefs.keySet());
            keys.addAll(assocDefs.keySet());
            keys.addAll(propKeys);
        }
        else
            keys = buildQNameKeys(propertyFilters);
        return buildQNameProperties(properties, keys);
    }

    private Map<String, Object> buildQNameProperties(Map<QName, Serializable> properties, Collection<QName> keys)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        for (QName key : keys)
        {
            Object value = convertValue(properties.get(key));
            String prefixedKey = key.toPrefixString(namespaceService);
            String strKey = prefixedKey.replace(PREFIX_SEPARATOR, "_");
            model.put(strKey, value);
        }
        return model;
    }

    private Object convertValue(Object value)
    {
        if(value == null
                || value instanceof Boolean
                || value instanceof Number
                || value instanceof String)
        {
            return value;
        }
        if(value instanceof Collection<?>)
        {
            Collection<?> collection = (Collection<?>)value;
            ArrayList<Object> results = new ArrayList<Object>(collection.size());
            for (Object obj : collection) 
            {
                results.add(convertValue(obj));
            }
            return results;
        }
        return DefaultTypeConverter.INSTANCE.convert(String.class, value);
    }

    private Collection<QName> buildQNameKeys(Collection<String> keys)
    {
        List<QName> qKeys = new ArrayList<QName>(keys.size());
        for (String key : keys) 
        {
            String prefixedName = key.replaceFirst("_", PREFIX_SEPARATOR);
            try
            {
                QName qKey = QName.createQName(prefixedName, namespaceService);
                qKeys.add(qKey);
            } catch(NamespaceException e)
            {
                throw new AlfrescoRuntimeException("Invalid property key: "+key, e);
            }
        }
        return qKeys;
    }

    private Map<String, Object> getPersonModel(Serializable nameSer)
    {
        if (!(nameSer instanceof String))
            return null;

        String name = (String) nameSer;
        NodeRef person = personService.getPerson(name);
        Map<QName, Serializable> properties = nodeService.getProperties(person);

        // TODO Person URL?
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(PERSON_USER_NAME, name);
        model.put(PERSON_FIRST_NAME, properties.get(ContentModel.PROP_FIRSTNAME));
        model.put(PERSON_LAST_NAME, properties.get(ContentModel.PROP_LASTNAME));
        return model;
    }

//    private String getURl(WorkflowPath path)
//    {
//        StringBuilder builder = new StringBuilder("api/workflow-instances/");
//        builder.append(path.instance.id);
//        builder.append("/paths/");
//        builder.append(path.id);
//        return builder.toString();
//    }

    private String getUrl(WorkflowTask task)
    {
        return "api/task-instance/" + task.id;
    }

    private String getUrl(WorkflowDefinition workflowDefinition)
    {
        return "api/workflow-definition/" + workflowDefinition.id;
    }

}
