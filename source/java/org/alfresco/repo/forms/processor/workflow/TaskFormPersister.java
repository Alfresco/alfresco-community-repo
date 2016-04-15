
package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.TaskUpdater;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
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
                BehaviourFilter behaviourFilter, Log logger)
    {
        super(itemData, namespaceService, dictionaryService, logger);
        WorkflowTask item = itemData.getItem();

        // make sure that the task is not already completed
        if (item.getState().equals(WorkflowTaskState.COMPLETED))
        {
            throw new AlfrescoRuntimeException("workflowtask.already.done.error");
        }

        // make sure the current user is able to edit the task
        if (!workflowService.isTaskEditable(item, authenticationService.getCurrentUserName()))
        {
            throw new AccessDeniedException("Failed to update task with id '" + item.getId() + "'.");
        }
        
        this.updater = new TaskUpdater(item.getId(), workflowService, nodeService, behaviourFilter);
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
