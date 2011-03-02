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
import java.util.Arrays;
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
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * @author Nick Smith
 *
 */
public class WorkflowModelBuilder
{
    public static final String PERSON_LAST_NAME = "lastName";
    public static final String PERSON_FIRST_NAME = "firstName";
    public static final String PERSON_USER_NAME = "userName";
    public static final String PERSON_AVATAR = "avatarUrl";

    public static final String TASK_PROPERTIES = "properties";
    public static final String TASK_OWNER = "owner";
    public static final String TASK_STATE = "state";
    public static final String TASK_DESCRIPTION = "description";
    public static final String TASK_TITLE = "title";
    public static final String TASK_NAME = "name";
    public static final String TASK_URL = "url";
    public static final String TASK_IS_POOLED = "isPooled";
    public static final String TASK_IS_EDITABLE = "isEditable";
    public static final String TASK_IS_REASSIGNABLE = "isReassignable";
    public static final String TASK_IS_CLAIMABLE = "isClaimable";
    public static final String TASK_IS_RELEASABLE = "isReleasable";
    public static final String TASK_ID = "id";
    public static final String TASK_PATH = "path";
    public static final String TASK_DEFINITION = "definition";
    public static final String TASK_OUTCOME = "outcome";

    public static final String TASK_DEFINITION_ID = "id";
    public static final String TASK_DEFINITION_URL = "url";
    public static final String TASK_DEFINITION_TYPE = "type";
    public static final String TASK_DEFINITION_NODE = "node";

    public static final String TASK_WORKFLOW_INSTANCE = "workflowInstance";
    
    public static final String TASK_WORKFLOW_INSTANCE_ID = "id";
    public static final String TASK_WORKFLOW_INSTANCE_URL = "url";
    public static final String TASK_WORKFLOW_INSTANCE_NAME = "name";
    public static final String TASK_WORKFLOW_INSTANCE_TITLE = "title";
    public static final String TASK_WORKFLOW_INSTANCE_DESCRIPTION = "description";
    public static final String TASK_WORKFLOW_INSTANCE_MESSAGE = "message";
    public static final String TASK_WORKFLOW_INSTANCE_IS_ACTIVE = "isActive";
    public static final String TASK_WORKFLOW_INSTANCE_START_DATE = "startDate";
    public static final String TASK_WORKFLOW_INSTANCE_DUE_DATE = "dueDate";
    public static final String TASK_WORKFLOW_INSTANCE_END_DATE = "endDate";
    public static final String TASK_WORKFLOW_INSTANCE_PRIORITY = "priority";
    public static final String TASK_WORKFLOW_INSTANCE_INITIATOR = "initiator";
    public static final String TASK_WORKFLOW_INSTANCE_CONTEXT = "context";
    public static final String TASK_WORKFLOW_INSTANCE_PACKAGE = "package";
    public static final String TASK_WORKFLOW_INSTANCE_START_TASK_INSTANCE_ID = "startTaskInstanceId";
    public static final String TASK_WORKFLOW_INSTANCE_DEFINITION = "definition";
    public static final String TASK_WORKFLOW_INSTANCE_TASKS = "tasks";
    public static final String TASK_WORKFLOW_INSTANCE_DEFINITION_URL = "definitionUrl";

    public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_USERNAME = "userName";
    public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_FIRSTNAME = "firstName";
    public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_LASTNAME = "lastName";
    
    public static final String TYPE_DEFINITION_NAME = "name";
    public static final String TYPE_DEFINITION_TITLE = "title";
    public static final String TYPE_DEFINITION_DESCRIPTION = "description";
    public static final String TYPE_DEFINITION_URL = "url";

    public static final String WORKFLOW_NODE_NAME = "name";
    public static final String WORKFLOW_NODE_TITLE = "title";
    public static final String WORKFLOW_NODE_DESCRIPTION = "description";
    public static final String WORKFLOW_NODE_IS_TASK_NODE = "isTaskNode";
    public static final String WORKFLOW_NODE_TRANSITIONS = "transitions";

    public static final String WORKFLOW_NODE_TRANSITION_ID = "id";
    public static final String WORKFLOW_NODE_TRANSITION_TITLE = "title";
    public static final String WORKFLOW_NODE_TRANSITION_DESCRIPTION = "description";
    public static final String WORKFLOW_NODE_TRANSITION_IS_DEFAULT = "isDefault";
    public static final String WORKFLOW_NODE_TRANSITION_IS_HIDDEN = "isHidden";

    public static final String WORKFLOW_DEFINITION_ID = "id";
    public static final String WORKFLOW_DEFINITION_URL = "url";
    public static final String WORKFLOW_DEFINITION_NAME = "name";
    public static final String WORKFLOW_DEFINITION_TITLE = "title";
    public static final String WORKFLOW_DEFINITION_DESCRIPTION = "description";
    public static final String WORKFLOW_DEFINITION_VERSION = "version";
    public static final String WORKFLOW_DEFINITION_START_TASK_DEFINITION_URL = "startTaskDefinitionUrl";
    public static final String WORKFLOW_DEFINITION_START_TASK_DEFINITION_TYPE = "startTaskDefinitionType";
    public static final String WORKFLOW_DEFINITION_TASK_DEFINITIONS = "taskDefinitions";

    public static final String PAGING_TOTAL_ITEMS = "totalItems";
    public static final String PAGING_MAX_ITEMS = "maxItems";
    public static final String PAGING_SKIP_COUNT = "skipCount";

    private static final String PREFIX_SEPARATOR = Character.toString(QName.NAMESPACE_PREFIX);

    private final NamespaceService namespaceService;
    private final NodeService nodeService;
    private final PersonService personService;
    private final WorkflowService workflowService;
    private final AuthenticationService authenticationService;

    public WorkflowModelBuilder(NamespaceService namespaceService, NodeService nodeService, 
                AuthenticationService authenticationService, PersonService personService, 
                WorkflowService workflowService)
    {
        this.namespaceService = namespaceService;
        this.nodeService = nodeService;
        this.personService = personService;
        this.workflowService = workflowService;
        this.authenticationService = authenticationService;
    }

    /**
     * Returns a simple representation of a {@link WorkflowTask}.
     * @param task The task to be represented.
     * @param propertyFilters Specify which properties to include.
     * @return
     */
    public Map<String, Object> buildSimple(WorkflowTask task, Collection<String> propertyFilters)
    {
        // get current username
        String currentUser = this.authenticationService.getCurrentUserName();
        
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put(TASK_ID, task.getId());
        model.put(TASK_URL, getUrl(task));
        model.put(TASK_NAME, task.getName());
        model.put(TASK_TITLE, task.getTitle());
        model.put(TASK_DESCRIPTION, task.getDescription());
        model.put(TASK_STATE, task.getState().name());
        model.put(TASK_PATH, getUrl(task.getPath()));
        model.put(TASK_OUTCOME, getOutcome(task));
        model.put(TASK_IS_POOLED, isPooled(task.getProperties()));
        model.put(TASK_IS_EDITABLE, this.workflowService.isTaskEditable(task, currentUser));
        model.put(TASK_IS_REASSIGNABLE, this.workflowService.isTaskReassignable(task, currentUser));
        model.put(TASK_IS_CLAIMABLE, this.workflowService.isTaskClaimable(task, currentUser));
        model.put(TASK_IS_RELEASABLE, this.workflowService.isTaskReleasable(task, currentUser));
        
        Serializable owner = task.getProperties().get(ContentModel.PROP_OWNER);
        model.put(TASK_OWNER, getPersonModel(owner));

        // task properties
        model.put(TASK_PROPERTIES, buildProperties(task, propertyFilters));
        
        // workflow instance part
        model.put(TASK_WORKFLOW_INSTANCE, buildSimple(task.getPath().getInstance()));
        
        return model;
    }

    /**
     * Returns a detailed representation of a {@link WorkflowTask}.
     * @param workflowTask The task to be represented.     
     * @return
     */
    public Map<String, Object> buildDetailed(WorkflowTask workflowTask)
    {
        Map<String, Object> model = buildSimple(workflowTask, null);

        // definition part
        model.put(TASK_DEFINITION, buildTaskDefinition(workflowTask.getDefinition(), workflowTask));

        return model;
    }

    /**
     * Returns a simple representation of a {@link WorkflowInstance}.
     * @param workflowInstance The workflow instance to be represented.          
     * @return
     */
    public Map<String, Object> buildSimple(WorkflowInstance workflowInstance)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(TASK_WORKFLOW_INSTANCE_ID, workflowInstance.getId());
        model.put(TASK_WORKFLOW_INSTANCE_URL, getUrl(workflowInstance));
        model.put(TASK_WORKFLOW_INSTANCE_NAME, workflowInstance.getDefinition().getName());
        model.put(TASK_WORKFLOW_INSTANCE_TITLE, workflowInstance.getDefinition().getTitle());
        model.put(TASK_WORKFLOW_INSTANCE_DESCRIPTION, workflowInstance.getDefinition().getDescription());
        model.put(TASK_WORKFLOW_INSTANCE_MESSAGE, workflowInstance.getDescription());
        model.put(TASK_WORKFLOW_INSTANCE_IS_ACTIVE, workflowInstance.isActive());
        model.put(TASK_WORKFLOW_INSTANCE_PRIORITY, workflowInstance.getPriority());
        model.put(TASK_WORKFLOW_INSTANCE_DEFINITION_URL, getUrl(workflowInstance.getDefinition()));

        if (workflowInstance.getWorkflowPackage() != null)
        {
            model.put(TASK_WORKFLOW_INSTANCE_PACKAGE, workflowInstance.getWorkflowPackage().toString());
        }
        
        if (workflowInstance.getContext() != null)
        {
            model.put(TASK_WORKFLOW_INSTANCE_CONTEXT, workflowInstance.getContext().toString());
        }
        
        if (workflowInstance.getStartDate() == null)
        {
            model.put(TASK_WORKFLOW_INSTANCE_START_DATE, workflowInstance.getStartDate());
        }
        else
        {
            model.put(TASK_WORKFLOW_INSTANCE_START_DATE, ISO8601DateFormat.format(workflowInstance.getStartDate()));
        }
        
        if (workflowInstance.getDueDate() == null)
        {
            model.put(TASK_WORKFLOW_INSTANCE_DUE_DATE, workflowInstance.getDueDate());
        }
        else
        {
            model.put(TASK_WORKFLOW_INSTANCE_DUE_DATE, ISO8601DateFormat.format(workflowInstance.getDueDate()));
        }

        if (workflowInstance.getEndDate() == null)
        {
            model.put(TASK_WORKFLOW_INSTANCE_END_DATE, workflowInstance.getEndDate());
        }
        else
        {
            model.put(TASK_WORKFLOW_INSTANCE_END_DATE, ISO8601DateFormat.format(workflowInstance.getEndDate()));
        }

        if (workflowInstance.getInitiator() == null || !nodeService.exists(workflowInstance.getInitiator()))
        {
            model.put(TASK_WORKFLOW_INSTANCE_INITIATOR, null);
        }
        else
        {
            model.put(TASK_WORKFLOW_INSTANCE_INITIATOR, getPersonModel(nodeService.getProperty(workflowInstance.getInitiator(), ContentModel.PROP_USERNAME)));
        }

        return model;
    }
    
    /**
     * Returns a detailed representation of a {@link WorkflowInstance}.
     * @param workflowInstance The workflow instance to be represented.
     * @param includeTasks should we include task in model?     
     * @return
     */
    public Map<String, Object> buildDetailed(WorkflowInstance workflowInstance, boolean includeTasks)
    {
        Map<String, Object> model = buildSimple(workflowInstance);

        Serializable startTaskId = null;
        WorkflowTask startTask = workflowService.getStartTask(workflowInstance.getId());
        if (startTask != null)
        {
            startTaskId = startTask.getId();
        }
        
        model.put(TASK_WORKFLOW_INSTANCE_START_TASK_INSTANCE_ID, startTaskId);
        model.put(TASK_WORKFLOW_INSTANCE_DEFINITION, buildDetailed(workflowInstance.getDefinition()));

        if (includeTasks)
        {
            // get all tasks for workflow
            WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
            tasksQuery.setTaskState(null);
            tasksQuery.setActive(null);
            tasksQuery.setProcessId(workflowInstance.getId());
            List<WorkflowTask> tasks = workflowService.queryTasks(tasksQuery);

            ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>(tasks.size());

            for (WorkflowTask task : tasks)
            {
                results.add(buildSimple(task, null));
            }

            model.put(TASK_WORKFLOW_INSTANCE_TASKS, results);
        }

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

        model.put(WORKFLOW_DEFINITION_ID, workflowDefinition.getId());
        model.put(WORKFLOW_DEFINITION_URL, getUrl(workflowDefinition));
        model.put(WORKFLOW_DEFINITION_NAME, workflowDefinition.getName());
        model.put(WORKFLOW_DEFINITION_TITLE, workflowDefinition.getTitle());
        model.put(WORKFLOW_DEFINITION_DESCRIPTION, workflowDefinition.getDescription());
        model.put(WORKFLOW_DEFINITION_VERSION, workflowDefinition.getVersion());

        return model;
    }

    /**
     * Returns a detailed representation of a {@link WorkflowDefinition}.
     * 
     * @param workflowDefinition the WorkflowDefinition object to be represented.
     * @return
     */
    public Map<String, Object> buildDetailed(WorkflowDefinition workflowDefinition)
    {
        Map<String, Object> model = buildSimple(workflowDefinition);

        model.put(WORKFLOW_DEFINITION_START_TASK_DEFINITION_URL, getUrl(workflowDefinition.getStartTaskDefinition().getMetadata()));
        model.put(WORKFLOW_DEFINITION_START_TASK_DEFINITION_TYPE, workflowDefinition.getStartTaskDefinition().getMetadata().getName());

        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (WorkflowTaskDefinition taskDefinition : workflowService.getTaskDefinitions(workflowDefinition.getId()))
        {
            if (taskDefinition.getId().equals(workflowDefinition.getStartTaskDefinition().getId()))
            {
                continue;
            }

            Map<String, Object> result = new HashMap<String, Object>();

            result.put(TASK_DEFINITION_URL, getUrl(taskDefinition.getMetadata()));
            result.put(TASK_DEFINITION_TYPE, taskDefinition.getMetadata().getName());

            results.add(result);
        }
        model.put(WORKFLOW_DEFINITION_TASK_DEFINITIONS, results);

        return model;
    }
    
    /**
     * Returns representation of paging object
     * 
     * @param totalItems all count of object
     * @param maxItems max count of object that should be returned
     * @param skipCount count of skipped objects
     * @return
     */
    public Map<String, Object> buildPaging(int totalItems, int maxItems, int skipCount)
    {
        HashMap<String, Object> model = new HashMap<String, Object>();

        model.put(PAGING_TOTAL_ITEMS, totalItems);
        model.put(PAGING_MAX_ITEMS, maxItems);
        model.put(PAGING_SKIP_COUNT, skipCount);
        
        return model;
    }

    private Object isPooled(Map<QName, Serializable> properties)
    {
        Collection<?> actors = (Collection<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
        return actors != null && !actors.isEmpty();
    }

    private Map<String, Object> buildProperties(WorkflowTask task, Collection<String> propertyFilters)
    {
        Map<QName, Serializable> properties = task.getProperties();
        Collection<QName> keys;
        if (propertyFilters == null || propertyFilters.size() == 0)
        {
            TypeDefinition taskType = task.getDefinition().getMetadata();
            Map<QName, PropertyDefinition> propDefs = taskType.getProperties();
            Map<QName, AssociationDefinition> assocDefs = taskType.getAssociations();
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
        if (value == null || value instanceof Boolean || value instanceof Number || value instanceof String)
        {
            return value;
        }
        
        if (value instanceof Collection<?>)
        {
            Collection<?> collection = (Collection<?>) value;
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
            }
            catch (NamespaceException e)
            {
                throw new AlfrescoRuntimeException("Invalid property key: " + key, e);
            }
        }
        return qKeys;
    }

    private Map<String, Object> getPersonModel(Serializable nameSer)
    {
        if (!(nameSer instanceof String))
            return null;

        String name = (String) nameSer;

        // TODO Person URL?
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(PERSON_USER_NAME, name);
        
        if (personService.personExists(name))
        {
            NodeRef person = personService.getPerson(name);
            Map<QName, Serializable> properties = nodeService.getProperties(person);
            model.put(PERSON_FIRST_NAME, properties.get(ContentModel.PROP_FIRSTNAME));
            model.put(PERSON_LAST_NAME, properties.get(ContentModel.PROP_LASTNAME));
            
            // add the avatar, id present
            List<AssociationRef> avatar = nodeService.getTargetAssocs(person, ContentModel.ASSOC_AVATAR);
            if (avatar != null && !avatar.isEmpty())
            {
                model.put(PERSON_AVATAR, getAvatarUrl(avatar.get(0).getTargetRef()));
            }
        }
        
        return model;
    }

    private Map<String, Object> buildTaskDefinition(WorkflowTaskDefinition workflowTaskDefinition, WorkflowTask workflowTask)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(TASK_DEFINITION_ID, workflowTaskDefinition.getId());
        model.put(TASK_DEFINITION_URL, getUrl(workflowTaskDefinition));
        model.put(TASK_DEFINITION_TYPE, buildTypeDefinition(workflowTaskDefinition.getMetadata()));
        model.put(TASK_DEFINITION_NODE, buildWorkflowNode(workflowTaskDefinition.getNode(), workflowTask));

        return model;
    }

    private Map<String, Object> buildTypeDefinition(TypeDefinition typeDefinition)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(TYPE_DEFINITION_NAME, typeDefinition.getName());
        model.put(TYPE_DEFINITION_TITLE, typeDefinition.getTitle());
        model.put(TYPE_DEFINITION_DESCRIPTION, typeDefinition.getDescription());
        model.put(TYPE_DEFINITION_URL, getUrl(typeDefinition));

        return model;
    }

    private Map<String, Object> buildWorkflowNode(WorkflowNode workflowNode, WorkflowTask workflowTask)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(WORKFLOW_NODE_NAME, workflowNode.getName());
        model.put(WORKFLOW_NODE_TITLE, workflowNode.getTitle());
        model.put(WORKFLOW_NODE_DESCRIPTION, workflowNode.getDescription());
        model.put(WORKFLOW_NODE_IS_TASK_NODE, workflowNode.isTaskNode());

        List<Map<String, Object>> transitions = new ArrayList<Map<String, Object>>();
        List<?> hiddenTransitions = getHiddenTransitions(workflowTask.getProperties());
        for (WorkflowTransition workflowTransition : workflowNode.getTransitions())
        {
            Map<String, Object> transitionModel = buildTransition(workflowTransition, hiddenTransitions);
            transitions.add(transitionModel);
        }
        model.put(WORKFLOW_NODE_TRANSITIONS, transitions);
        return model;
    }

    private Map<String, Object> buildTransition(WorkflowTransition workflowTransition, List<?> hiddenTransitions)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        String id = workflowTransition.getId();
        model.put(WORKFLOW_NODE_TRANSITION_ID, id == null ? "" : id);
        model.put(WORKFLOW_NODE_TRANSITION_TITLE, workflowTransition.getTitle());
        model.put(WORKFLOW_NODE_TRANSITION_DESCRIPTION, workflowTransition.getDescription());
        model.put(WORKFLOW_NODE_TRANSITION_IS_DEFAULT, workflowTransition.isDefault());
        model.put(WORKFLOW_NODE_TRANSITION_IS_HIDDEN, isHiddenTransition(id, hiddenTransitions));
        return model;
    }

    /**
     * @param properties
     * @return
     */
    private List<?> getHiddenTransitions(Map<QName, Serializable> properties)
    {
        Serializable hiddenSer = properties.get(WorkflowModel.PROP_HIDDEN_TRANSITIONS);
        if (hiddenSer instanceof List<?>)
        {
            return (List<?>) hiddenSer;
        }
        else if (hiddenSer instanceof String)
        {
            String hiddenStr = (String) hiddenSer;
            return Arrays.asList(hiddenStr.split(","));
        }
        return null;
    }
    
    private boolean isHiddenTransition(String transitionId, List<?> hiddenTransitions)
    {
        if (hiddenTransitions == null)
            return false;
        
        return hiddenTransitions.contains(transitionId);
    }

    private String getOutcome(WorkflowTask task)
    {
        String outcomeLabel = null;
        
        // there will only be an outcome if the task is completed
        if (task.getState().equals(WorkflowTaskState.COMPLETED))
        {
            String outcomeId = (String)task.getProperties().get(WorkflowModel.PROP_OUTCOME);
            if (outcomeId != null)
            {
                // find the transition with the matching id and get the label
                WorkflowTransition[] transitions = task.getDefinition().getNode().getTransitions();
                for (WorkflowTransition transition : transitions)
                {
                    if (transition.getId().equals(outcomeId))
                    {
                        outcomeLabel = transition.getTitle();
                        break;
                    }
                }
                if(outcomeLabel == null)
                {
                	// TODO: is this okay -> no real transitions exist for activiti
                	outcomeLabel = outcomeId;
                }
            }
        }
        
        return outcomeLabel;
    }

    private String getUrl(WorkflowTask task)
    {
        return "api/task-instances/" + task.getId();
    }

    private String getUrl(WorkflowDefinition workflowDefinition)
    {
        return "api/workflow-definitions/" + workflowDefinition.getId();
    }

    private String getUrl(WorkflowTaskDefinition workflowTaskDefinition)
    {
        return "api/task-definitions/" + workflowTaskDefinition.getId();
    }

    private String getUrl(TypeDefinition typeDefinition)
    {
        return "api/classes/" + typeDefinition.getName().toPrefixString().replace(PREFIX_SEPARATOR, "_");
    }

    private String getUrl(WorkflowPath path)
    {
        return "api/workflow-paths/" + path.getId();
    }

    private String getUrl(WorkflowInstance workflowInstance)
    {
        return "api/workflow-instances/" + workflowInstance.getId();
    }

    private String getAvatarUrl(NodeRef avatarRef)
    {
        return "api/node/" + avatarRef.toString().replace("://", "/") + "/content/thumbnails/avatar";
    }
}
