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

package org.alfresco.repo.invitation.site;

import java.util.Map;

import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ExecutionContext;

public class SendInviteAction extends AbstractInvitationAction
{
    private static final long serialVersionUID = 8133039174866049136L;

    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext context) throws Exception
    {
        ContextInstance contextInstance = context.getContextInstance();
        long processId = contextInstance.getProcessInstance().getId();
        String inviteId = JBPMEngine.ENGINE_ID + "$" + processId;
        Map<String, Object> executionVariables = contextInstance.getVariables();
        inviteHelper.sendNominatedInvitation(inviteId, executionVariables);
    }

}
