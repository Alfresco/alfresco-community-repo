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

package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.processor.FieldProcessorRegistry;
import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * FormProcessor implementation for workflow tasks.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class TaskFormProcessor extends AbstractWorkflowFormProcessor<WorkflowTask, WorkflowTask>
{
    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(TaskFormProcessor.class);

    protected AuthenticationService authenticationService;
    protected PersonService personService;
    
    // Constructor for Spring
    public TaskFormProcessor()
    {
        super();
    }

    // Constructor for tests.
    public TaskFormProcessor(WorkflowService workflowService, NamespaceService namespaceService,
            DictionaryService dictionaryService, AuthenticationService authenticationService,
            PersonService personService, FieldProcessorRegistry fieldProcessorRegistry)
    {
        this.workflowService = workflowService;
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;
        this.authenticationService = authenticationService;
        this.personService = personService;
        this.fieldProcessorRegistry = fieldProcessorRegistry;
    }

    /**
     * Sets the authentication service
     * 
     * @param authenticationService The AuthenticationService instance
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /**
     * Sets the person service
     * 
     * @param personService The PersonService instance
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
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
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemType(java.lang.Object)
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
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemURI(java.lang.Object)
     */
    @Override
    protected String getItemURI(WorkflowTask item)
    {
        return "api/task-instances/" + item.id;
    }

    /*
     * @see org.alfresco.repo.forms.processor.task.ContentModelFormProcessor#getLogger()
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
        Map<String, Object> values = new HashMap<String, Object>(2);
        values.put(TransitionFieldProcessor.KEY, getTransitionValues(item));
        values.put(PackageItemsFieldProcessor.KEY, getPackageItemValues(item));
        values.put(MessageFieldProcessor.KEY, getMessageValue(item));
        values.put(TaskOwnerFieldProcessor.KEY, getTaskOwnerValue(item));
        return values;
    }

    /**
     * @param item
     * @return
     */
    private Object getPackageItemValues(WorkflowTask task)
    {
        List<NodeRef> items = workflowService.getPackageContents(task.getId());
        ArrayList<String> results = new ArrayList<String>(items.size());
        for (NodeRef item : items)
        {
            results.add(item.toString());
        }
        return results;
    }
    
    private String getMessageValue(WorkflowTask task)
    {
        String message = I18NUtil.getMessage(MessageFieldProcessor.MSG_VALUE_NONE);
        
        String description = (String)task.getProperties().get(WorkflowModel.PROP_DESCRIPTION);
        if (description != null)
        {
            String taskTitle = task.getTitle();
            if (taskTitle == null || !taskTitle.equals(description))
            {
                message = description;
            }
        }
        
        return message;
    }
    
    private String getTaskOwnerValue(WorkflowTask task)
    {
        String owner = (String)task.getProperties().get(ContentModel.PROP_OWNER);
        
        if (owner == null || owner.length() == 0)
        {
            return null;
        }
        
        return buildTaskOwnerString(owner);
    }
    
    private String buildTaskOwnerString(String ownerUsername)
    {
        StringBuilder builder = new StringBuilder(ownerUsername);
        
        // get the person node
        NodeRef ownerNodeRef = null;
        try
        {
            ownerNodeRef = this.personService.getPerson(ownerUsername);
        }
        catch (NoSuchPersonException nspe)
        {
            // just return the username if the user doesn't exist
        }
        
        if (ownerNodeRef != null)
        {
            Map<QName, Serializable> personProps = this.nodeService.getProperties(ownerNodeRef);
            
            builder.append("|");
            builder.append(personProps.containsKey(ContentModel.PROP_FIRSTNAME) ? 
                        personProps.get(ContentModel.PROP_FIRSTNAME) : "");
            builder.append("|");
            builder.append(personProps.containsKey(ContentModel.PROP_LASTNAME) ? 
                        personProps.get(ContentModel.PROP_LASTNAME) : "");
        }
        
        return builder.toString();
    }

    private String getTransitionValues(WorkflowTask item)
    {
        WorkflowTransition[] transitions = item.definition.node.transitions;
        
        if (transitions == null || transitions.length == 0)
        {
            return "";
        }
        
        return buildTransitionString(item, transitions);
    }

    private String buildTransitionString(WorkflowTask item, WorkflowTransition[] transitions)
    {
        StringBuilder builder = new StringBuilder();
        List<?> hiddenStr = getHiddenTransitions(item);
        for (WorkflowTransition transition : transitions)
        {
            String transId = transition.getId();
            if (hiddenStr.contains(transId) == false)
            {
                builder.append(transId != null ? transId : "");
                builder.append("|");
                builder.append(transition.getTitle());
                builder.append(",");
            }
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private List<String> getHiddenTransitions(WorkflowTask task)
    {
        Serializable hiddenValues = task.getProperties().get(WorkflowModel.PROP_HIDDEN_TRANSITIONS);
        if (hiddenValues != null)
        {
            if (hiddenValues instanceof List<?>)
            {
                return (List<String>) hiddenValues;
            }
            else if (hiddenValues instanceof String && ((String)hiddenValues).length() > 0)
            {
                return Arrays.asList(((String)hiddenValues).split(","));
            }
        }
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.AbstractWorkflowFormProcessor#makeFormPersister(java.lang.Object)
     */
    @Override
    protected ContentModelFormPersister<WorkflowTask> makeFormPersister(WorkflowTask item)
    {
        ContentModelItemData<WorkflowTask> itemData = makeItemData(item);
        return new TaskFormPersister(itemData, namespaceService, dictionaryService, 
                    workflowService, nodeService, authenticationService, behaviourFilter, LOGGER);
    }
}
