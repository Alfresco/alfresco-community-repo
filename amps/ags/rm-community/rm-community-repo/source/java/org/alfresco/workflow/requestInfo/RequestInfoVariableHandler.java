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
