/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
