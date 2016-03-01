 
package org.alfresco.workflow.requestInfo;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Request info workflow notifier.
 * After the pooled task has been finished the initiator of the workflow will
 * get a task to verify the information. The initiator will also receive an email.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RequestInfoNotifier implements TaskListener
{
    private static final long serialVersionUID = -7169400062409052556L;

    /**
     * @see org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.delegate.DelegateTask)
     */
    @Override
    public void notify(DelegateTask delegateTask)
    {
        ParameterCheck.mandatory("delegateTask", delegateTask);

        // Get the record name
        String recordName = RequestInfoUtils.getRecordName(delegateTask);

        // Set the workflow description for the task
        delegateTask.setVariable("bpm_workflowDescription", getWorkflowDescription(recordName));

        // Assign the task to the initiator
        String initiator = RequestInfoUtils.getInitiator(delegateTask);
        delegateTask.setAssignee(initiator);
    }

    /**
     * Helper method for building the workflow description
     *
     * @param recordName The name of the record
     * @return Returns the workflow description
     */
    private String getWorkflowDescription(String recordName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(I18NUtil.getMessage("activitiReviewPooled.workflow.info.provided"));
        sb.append(" '");
        sb.append(recordName);
        sb.append("'");
        return  sb.toString();
    }
}
