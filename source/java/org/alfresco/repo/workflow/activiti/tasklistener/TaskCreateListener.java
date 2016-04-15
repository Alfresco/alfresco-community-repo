
package org.alfresco.repo.workflow.activiti.tasklistener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.FormData;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLinkType;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;

/**
 * Tasklistener that is notified when a task is created. This will set all
 * default properties for this task.
 * 
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class TaskCreateListener implements TaskListener
{
    private static final long serialVersionUID = 1L;
    
    private ActivitiPropertyConverter propertyConverter;
    
    @Override
    public void notify(DelegateTask task)
    {
        // Set all default properties, based on the type-definition
        propertyConverter.setDefaultTaskProperties(task);

        String taskFormKey = getFormKey(task);
        
        // Fetch definition and extract name again. Possible that the default is used if the provided is missing
        TypeDefinition typeDefinition = propertyConverter.getWorkflowObjectFactory().getTaskTypeDefinition(taskFormKey, false);
        taskFormKey = typeDefinition.getName().toPrefixString();
        
        // The taskDefinition key is set as a variable in order to be available
        // in the history
        task.setVariableLocal(ActivitiConstants.PROP_TASK_FORM_KEY, taskFormKey);
        
        // Add process initiator as involved person
        ActivitiScriptNode initiatorNode = (ActivitiScriptNode) task.getExecution().getVariable(WorkflowConstants.PROP_INITIATOR);
        if(initiatorNode != null) {
            task.addUserIdentityLink((String) initiatorNode.getProperties().get(ContentModel.PROP_USERNAME.toPrefixString()), IdentityLinkType.STARTER);
        }
    }

    private String getFormKey(DelegateTask task)
    {
        FormData formData = null;
        TaskEntity taskEntity = (TaskEntity) task;
        TaskFormHandler taskFormHandler = taskEntity.getTaskDefinition().getTaskFormHandler();
        if (taskFormHandler != null)
        {
            formData = taskFormHandler.createTaskForm(taskEntity);
            if (formData != null) { return formData.getFormKey(); }
        }
        return null;
    }
    
    /**
     * @param propertyConverter the propertyConverter to set
     */
    public void setPropertyConverter(ActivitiPropertyConverter propertyConverter)
    {
        this.propertyConverter = propertyConverter;
    }
}