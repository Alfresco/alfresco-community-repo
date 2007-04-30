/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow.jbpm;

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
            if (!(result instanceof Date))
            {
                throw new WorkflowException("duedate expression must evaluate to a date");
            }
            dueDate = (Date)result;
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
