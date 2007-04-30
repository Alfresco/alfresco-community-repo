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

    /* (non-Javadoc)
     * @see org.jbpm.job.Job#execute(org.jbpm.JbpmContext)
     */
    @Override
    public boolean execute(final JbpmContext jbpmContext)
        throws Exception
    {
        boolean executeResult = false;
        
        // establish authentication context
        String username = null;
        TaskInstance taskInstance = getTaskInstance();
        if (taskInstance != null)
        {
            String actorId = taskInstance.getActorId();
            if (actorId != null && actorId.length() > 0)
            {
                username = actorId;
            }
        }
        
        // execute timer
        if (username == null)
        {
            executeResult = super.execute(jbpmContext);
        }
        else
        {
            executeResult = AuthenticationUtil.runAs(new RunAsWork<Boolean>()
            {
                @SuppressWarnings("synthetic-access")
                public Boolean doWork() throws Exception
                {
                    return AlfrescoTimer.super.execute(jbpmContext);
                }
            }, username);
        }
        
        return executeResult;
    }

}
