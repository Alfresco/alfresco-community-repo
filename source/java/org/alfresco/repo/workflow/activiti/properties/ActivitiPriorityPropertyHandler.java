
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
public class ActivitiPriorityPropertyHandler extends ActivitiTaskPropertyHandler
{
    /**
    * {@inheritDoc}
    */
    
    @Override
    protected Object handleTaskProperty(Task task, TypeDefinition type, QName key, Serializable value)
    {
        int priority;
        // ACE-3121: Workflow Admin Console: Cannot change priority for activiti
        // It could be a String that converts to an int, like when coming from WorkflowInterpreter.java
        if (value instanceof String)
        {
            try
            {
                priority = Integer.parseInt((String) value);
            }
            catch (NumberFormatException e)
            {
                throw getInvalidPropertyValueException(key, value);
            }
        }
        else
        {
            checkType(key, value, Integer.class);
            priority = (Integer) value;
        }

        // Priority value validation not performed to allow for future model changes
        task.setPriority(priority);

        return DO_NOT_ADD;
    }

    /**
    * {@inheritDoc}
    */
    
    @Override
    protected Object handleDelegateTaskProperty(DelegateTask task, TypeDefinition type, QName key, Serializable value)
    {
        checkType(key, value, Integer.class);
        task.setPriority((Integer) value);
        return DO_NOT_ADD;
    }

    /**
    * {@inheritDoc}
    */
    
    @Override
    protected QName getKey()
    {
        return WorkflowModel.PROP_PRIORITY;
    }
}
