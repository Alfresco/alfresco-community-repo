/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.workflow.jbpm;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.workflow.WorkflowException;
import org.jbpm.calendar.BusinessCalendar;
import org.jbpm.calendar.Duration;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.job.Timer;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.scheduler.def.CreateTimerAction;


/**
 * Extended Create Timer action for supporting Alfresco implemented timers.
 * 
 * Alfresco timer supports ability to provide due date expression that can
 * evaluate to a date. 
 * 
 * @author davidc
 */
public class AlfrescoCreateTimerAction extends CreateTimerAction
{
    private static final long serialVersionUID = -7427571820130058416L;
    protected static BusinessCalendar businessCalendar = new BusinessCalendar(); 
    
    
    /* (non-Javadoc)
     * @see org.jbpm.scheduler.def.CreateTimerAction#createTimer(org.jbpm.graph.exe.ExecutionContext)
     */
    @Override
    protected Timer createTimer(ExecutionContext executionContext)
    {
        Date dueDate = null;
        String dueDateExpression = getDueDate();
        if (dueDateExpression.startsWith("#{"))
        {
            Object result = JbpmExpressionEvaluator.evaluate(dueDateExpression, executionContext);
            if (result instanceof Date)
            {
                dueDate = (Date)result;
            }
            else if(result instanceof Calendar)
            {
                dueDate = ((Calendar)result).getTime();
            }
            else
            {
                throw new WorkflowException("duedate expression must evaluate to a date");
            }
        }
        else
        {
            Duration duration = new Duration(getDueDate());
            dueDate = businessCalendar.add(new Date(), duration);            
        }
        
        Timer timer = new AlfrescoTimer(executionContext.getToken());
        timer.setName(getTimerName());
        timer.setRepeat(getRepeat());
        timer.setDueDate(dueDate);
        timer.setAction(getTimerAction());
        timer.setTransitionName(getTransitionName());
        timer.setGraphElement(executionContext.getEventSource());
        timer.setTaskInstance(executionContext.getTaskInstance());

        // if this action was executed for a graph element
        if ((getEvent() != null) && (getEvent().getGraphElement() != null))
        {
            GraphElement graphElement = getEvent().getGraphElement();
            try
            {
                executionContext.setTimer(timer);
                // fire the create timer event on the same graph element
                graphElement.fireEvent("timer-create", executionContext);
            } 
            finally
            {
                executionContext.setTimer(null);
            }
        }

        return timer;
    }

}
