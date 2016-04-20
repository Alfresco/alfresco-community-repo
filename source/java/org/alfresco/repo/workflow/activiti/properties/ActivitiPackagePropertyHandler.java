
package org.alfresco.repo.workflow.activiti.properties;

import java.io.Serializable;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiTaskPropertyHandler;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiPackagePropertyHandler extends ActivitiTaskPropertyHandler
{
    private static final String PCKG_KEY = "bpm_package";
    
    private RuntimeService runtimeService;
    
    public void setRuntimeService(RuntimeService runtimeService)
    {
        this.runtimeService = runtimeService;
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected Object handleTaskProperty(Task task, TypeDefinition type, QName key, Serializable value)
    {
        return handlePackage(value, task.getProcessInstanceId());
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected Object handleDelegateTaskProperty(DelegateTask task, TypeDefinition type, QName key, Serializable value)
    {
        return handlePackage(value, task.getProcessInstanceId());
    }

    private Object handlePackage(Serializable value, String processId)
    {
        Object currentPckg = runtimeService.getVariableLocal(processId, PCKG_KEY);
        // Do not change package if one already exists!
        if (currentPckg == null)
        {
            if (value instanceof NodeRef)
            {
                return nodeConverter.convertNode((NodeRef)value);
            }
            else
            {
                throw getInvalidPropertyValueException(WorkflowModel.ASSOC_PACKAGE, value);
            }
        }
        return DO_NOT_ADD;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected QName getKey()
    {
        return WorkflowModel.ASSOC_PACKAGE;
    }
}
