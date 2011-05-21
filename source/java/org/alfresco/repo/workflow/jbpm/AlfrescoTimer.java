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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Timer;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Extended JBPM Timer that provides Alfresco context.
 * 
 * NOTE: The action triggered by the timer is executed as the user assigned
 *       to the task associated with the timer.  If not associated with a
 *       task, the timer is executed unauthenticated.
 *       
 * @author davidc
 * @author Nick Smith
 */
public class AlfrescoTimer extends Timer
{
    private static final long serialVersionUID = -6618486175822866286L;

    /**
     * Construct
     */
    public AlfrescoTimer()
    {
        super();
    }

    /**
     * Construct
     * 
     * @param token
     */
    public AlfrescoTimer(Token token)
    {
        super(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final JbpmContext jbpmContext)
        throws Exception
    {
        // establish authentication context
        final TaskInstance taskInstance = getTaskInstance();
        String username = getActorId(taskInstance);
        
        // execute timer
        return AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                boolean deleteTimer = AlfrescoTimer.super.execute(jbpmContext);
                // End the task if timer does not repeat.
                // Note the order is a little odd here as the task will be ended
                // after the token has been signalled to move to the next node.
                if (deleteTimer
                    && taskInstance != null 
                    && taskInstance.isOpen())
                {
                    taskInstance.setSignalling(false);
                	taskInstance.end();
                }
                return deleteTimer;
            }
        }, username);
    }

    private String getActorId(TaskInstance taskInstance)
    {
        if (taskInstance != null)
        {
            String actorId = taskInstance.getActorId();
            if (actorId != null && actorId.length() > 0)
            {
                return actorId;
            }
        }
        return AuthenticationUtil.getSystemUserName();
    }

}
