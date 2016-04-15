
package org.alfresco.repo.workflow.activiti.properties;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiTaskPropertyHandler;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiDescriptionPropertyHandler extends ActivitiTaskPropertyHandler
{
    /**
    * {@inheritDoc}
    */   
    @Override
    protected Object handleTaskProperty(Task task, TypeDefinition type, QName key, Serializable value)
    {
        checkType(key, value, String.class);
        task.setDescription((String) value);
        return DO_NOT_ADD;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected Object handleDelegateTaskProperty(DelegateTask task, TypeDefinition type, QName key, Serializable value)
    {
        checkType(key, value, String.class);
        task.setDescription((String) value);
        return DO_NOT_ADD;
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected QName getKey()
    {
        return WorkflowModel.PROP_DESCRIPTION;
    }
}
