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
import org.jbpm.job.ExecuteNodeJob;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * @since 3.4
 * @author Nick Smith
 *
 */
public class AlfrescoExecuteNodeJob extends ExecuteNodeJob
{
    private static final long serialVersionUID = 6257575556379132535L;

    public AlfrescoExecuteNodeJob()
    {
        super();
    }

    public AlfrescoExecuteNodeJob(Token token)
    {
        super(token);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final JbpmContext jbpmContext) throws Exception
    {
        // establish authentication context
        final TaskInstance taskInstance = getTaskInstance();
        String username = getActorId(taskInstance);
        
        // execute timer
        return AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
            @Override
            public Boolean doWork() throws Exception
            {
                return AlfrescoExecuteNodeJob.super.execute(jbpmContext);
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
