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
package org.alfresco.repo.invitation;


import java.util.Map;

import org.alfresco.repo.invitation.site.AbstractInvitationAction;
import org.jbpm.graph.exe.ExecutionContext;

public class ModeratedActionApprove extends AbstractInvitationAction
{
    private static final long serialVersionUID = 4377660284993206875L;
    
    /**
     * {@inheritDoc}
     **/
    @SuppressWarnings("unchecked")
    public void execute(ExecutionContext executionContext) throws Exception
    {
        Map<String, Object> variables = executionContext.getContextInstance().getVariables();
        String siteName = (String) variables.get(WorkflowModelModeratedInvitation.wfVarResourceName);
        String invitee = (String) variables.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        String role = (String) variables.get(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        String reviewer = (String) variables.get(WorkflowModelModeratedInvitation.wfVarReviewer);

        invitationService.approveModeratedInvitation(siteName, invitee, role, reviewer);
    }
}