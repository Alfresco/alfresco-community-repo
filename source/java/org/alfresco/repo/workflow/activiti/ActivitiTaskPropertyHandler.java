
package org.alfresco.repo.workflow.activiti;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.AbstractWorkflowPropertyHandler;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public abstract class ActivitiTaskPropertyHandler extends AbstractWorkflowPropertyHandler
{
    /**
    * {@inheritDoc}
    */
    public Object handleProperty(QName key, Serializable value, TypeDefinition type, Object object, Class<?> objectType)
    {
        if (DelegateTask.class.equals(objectType))
        {
            return handleDelegateTaskProperty((DelegateTask)object, type, key, value);
        }
        else if (Task.class.equals(objectType))
        {
            return handleTaskProperty((Task)object, type, key, value);
        }
        return handleProcessPropert(null, type, key, value);
    }

    /**
     * @param process Object
     * @param type TypeDefinition
     * @param key QName
     * @param value Serializable
     * @return Object
     */
    private Object handleProcessPropert(Object process, TypeDefinition type, QName key, Serializable value)
    {
        return handleDefaultProperty(process, type, key, value);
    }

    /**
     * Handles the property for a {@link Task}.
     * @param task Task
     * @param type TypeDefinition
     * @param key QName
     * @param value Serializable
     * @return Object
     */
    protected abstract Object handleTaskProperty(Task task, TypeDefinition type, QName key, Serializable value);

    /**
     * Handles the property for a {@link DelegateTask}.
     * @param task DelegateTask
     * @param value TypeDefinition
     * @param key QName
     * @param type Serializable
     * @return Object
     */
    protected abstract Object handleDelegateTaskProperty(DelegateTask task, TypeDefinition type, QName key, Serializable value);
}
