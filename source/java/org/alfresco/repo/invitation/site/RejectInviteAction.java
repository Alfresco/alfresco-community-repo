/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets completed
 * along the "reject" transition
 * 
 * @author Nick Smith
 */
public class RejectInviteAction extends AbstractInvitationAction
{
    private static final long serialVersionUID = 4377660284993206875L;

    /**
    * {@inheritDoc}
     */
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        // get the invitee user name
        String inviteeUserName = (String) executionContext.getVariable(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        String invitationId = JBPMEngine.ENGINE_ID + "$" + executionContext.getContextInstance().getProcessInstance().getId();
        inviteHelper.deleteAuthenticationIfUnused(inviteeUserName, invitationId);
    }
}
