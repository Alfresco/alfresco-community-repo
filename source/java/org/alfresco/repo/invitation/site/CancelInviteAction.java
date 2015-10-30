/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId;

import java.util.Map;

import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets cancelled
 * along the "cancel" transition
 * 
 * @author glen johnson at alfresco com
 */
public class CancelInviteAction extends AbstractInvitationAction
{
    private static final long serialVersionUID = -7603494389312553072L;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void execute(ExecutionContext executionContext) throws Exception
    {
        Map<String, Object> executionVariables = executionContext.getContextInstance().getVariables();
        String currentInviteId = JBPMEngine.ENGINE_ID + "$" + executionContext.getContextInstance().getProcessInstance().getId();

        // Get the invitee user name and site short name variables off the execution context
        String invitee = (String) executionVariables.get(wfVarInviteeUserName);
        String siteName = (String) executionVariables.get(wfVarResourceName);
        String inviteId = (String) executionVariables.get(wfVarWorkflowInstanceId);

        invitationService.cancelInvitation(siteName, invitee, inviteId, currentInviteId);
    }
}
