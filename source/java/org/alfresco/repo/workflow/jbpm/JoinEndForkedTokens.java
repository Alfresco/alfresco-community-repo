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

import java.util.Collection;
import java.util.Map;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.taskmgmt.exe.TaskMgmtInstance;

/**
 * Action Handler for ending child tokens / tasks
 * 
 * @author davidc
 */
public class JoinEndForkedTokens implements ActionHandler
{
    private static final long serialVersionUID = 8679390550752208189L;

    /**
     * Constructor
     */
    public JoinEndForkedTokens()
    {
        super();
    }

    /**
     * {@inheritDoc}
      */
    public void execute(ExecutionContext executionContext)
    {
        Token token = executionContext.getToken();
        Map<?, ?> childTokens = token.getActiveChildren();
        for (Object childToken : childTokens.values())
        {
            cancelToken(executionContext, (Token)childToken);
        }
    }

    /**
     * Cancel token
     * 
     * @param executionContext
     * @param token
     */
    protected void cancelToken(ExecutionContext executionContext, Token token)
    {
        // visit child tokens
        Map<?, ?> childTokens = token.getActiveChildren();
        for (Object childToken : childTokens.values())
        {
            cancelToken(executionContext, (Token)childToken);
        }

        // end token
        if (!token.hasEnded())
        {
            token.end(false);
        }
        
        // end any associated tasks
        cancelTokenTasks(executionContext, token);
    }

    /**
     * Cancel tasks associated with a token
     * 
     * @param executionContext
     * @param token
     */
    protected void cancelTokenTasks(ExecutionContext executionContext, Token token)
    {
        TaskMgmtInstance tms = executionContext.getTaskMgmtInstance();
        Collection<TaskInstance> tasks = tms.getUnfinishedTasks(token);
        for (Object task : tasks)
        {
            TaskInstance taskInstance = (TaskInstance)task;
            if (taskInstance.isBlocking())
            {
                taskInstance.setBlocking(false);
            }
            if (taskInstance.isSignalling())
            {
                taskInstance.setSignalling(false);
            }
            if (!taskInstance.hasEnded())
            {
                taskInstance.cancel();
            }
        }
    }
}
