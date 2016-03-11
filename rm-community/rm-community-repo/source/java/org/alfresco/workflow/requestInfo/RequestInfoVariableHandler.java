package org.alfresco.workflow.requestInfo;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.util.ParameterCheck;

/**
 * A variable handler for saving the task variables to the execution context.
 * Some of the information will be needed in other tasks (e.g. "rmwf_message").
 * This variable handler saves the local task variable to the execution context.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RequestInfoVariableHandler implements TaskListener
{
    private static final long serialVersionUID = -1759557028641631768L;

    /**
     * @see org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.delegate.DelegateTask)
     */
    @Override
    public void notify(DelegateTask delegateTask)
    {
        ParameterCheck.mandatory("delegateTask", delegateTask);

        // Save the variable from the task
        DelegateExecution execution = delegateTask.getExecution();
        execution.setVariable("rmwf_message", delegateTask.getVariable("rmwf_message"));
    }
}
