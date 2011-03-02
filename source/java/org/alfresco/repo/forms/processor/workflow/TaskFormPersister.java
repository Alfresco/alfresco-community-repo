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
import java.util.List;

import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.TaskUpdater;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;

/**
 * Helper class that persists a form, transitioning the task if requested.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class TaskFormPersister extends ContentModelFormPersister<WorkflowTask>
{
    private final TaskUpdater updater;
    private String transitionId = null;
    
    public TaskFormPersister(ContentModelItemData<WorkflowTask> itemData,
                NamespaceService namespaceService,
                DictionaryService dictionaryService,
                WorkflowService workflowService,
                NodeService nodeService,
                AuthenticationService authenticationService,
                Log logger)
    {
        super(itemData, namespaceService, dictionaryService, logger);
        WorkflowTask item = itemData.getItem();
        
        // make sure the current user is able to edit the task
        if (!workflowService.isTaskEditable(item, authenticationService.getCurrentUserName()))
        {
            throw new AccessDeniedException("Failed to update task with id '" + item.getId() + "'.");
        }
        
        this.updater = new TaskUpdater(item.id, workflowService, nodeService);
    }
    
    /**
    * {@inheritDoc}
     */
    @Override
    protected boolean addAssociation(QName qName, List<NodeRef> values)
    {
        updater.addAssociation(qName, values);
        return true;
    }

    /**
    * {@inheritDoc}
     */
    @Override
    protected boolean removeAssociation(QName qName, List<NodeRef> values)
    {
        updater.removeAssociation(qName, values);
        return true;
    }

    /**
    * {@inheritDoc}
     */
    @Override
    protected boolean updateProperty(QName qName, Serializable value)
    {
        updater.addProperty(qName, value);
        return true;
    }

    /**
    * {@inheritDoc}
     */
    @Override
    protected boolean addTransientAssociation(String fieldName, List<NodeRef> values)
    {
        if (PackageItemsFieldProcessor.KEY.equals(fieldName))
        {
            updater.addPackageItems(values);
            return true;
        }
        
        return false;
    }
    
    /**
    * {@inheritDoc}
     */
    @Override
    protected boolean removeTransientAssociation(String fieldName, List<NodeRef> values)
    {
        if (PackageItemsFieldProcessor.KEY.equals(fieldName))
        {
            updater.removePackageItems(values);
            return true;
        }
        
        return false;
    }
    
    /**
    * {@inheritDoc}
     */
    @Override
    protected boolean updateTransientProperty(String fieldName, FieldData fieldData)
    {
        if (TransitionFieldProcessor.KEY.equals(fieldName))
        {
            Object value = fieldData.getValue();
            if (value == null)
            {
                value = "";
            }
            
            transitionId = value.toString();
            return true;
        }
        
        return false;
    }
    
    /**
    * {@inheritDoc}
     */
    @Override
    public WorkflowTask persist()
    {
        if (transitionId == null)
        {
            // just update the task
            return updater.update();
        }
        else
        {
            // update the task first
            updater.update();
            
            if (transitionId.length() == 0)
            {
                // transition with the default transition
                return updater.transition();
            }
            else
            {
                // transition with the given transition id
                return updater.transition(transitionId);
            }
        }
    }    
}
