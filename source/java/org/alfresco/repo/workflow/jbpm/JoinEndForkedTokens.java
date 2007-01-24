/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
    }

    /* (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     */
    public void execute(ExecutionContext executionContext)
    {
        Token token = executionContext.getToken();
        Map childTokens = token.getActiveChildren();
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
        Map childTokens = token.getActiveChildren();
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
        Collection tasks = tms.getUnfinishedTasks(token);
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
