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
package org.alfresco.repo.invitation;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.invitation.site.InviteHelper;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;

/**
 * Activiti delegate that is executed when a invitation request has
 * been rejected.
 *
 * @author Frederik Heremans
 */
public class RejectInviteDelegate extends BaseJavaDelegate
{
    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        // Get the invitee user name
        final String inviteeUserName = (String) execution.getVariable(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        
        ServiceRegistry serviceRegistry = getServiceRegistry();
        WorkflowService workflowService = serviceRegistry.getWorkflowService();
        PersonService personService = serviceRegistry.getPersonService();
        MutableAuthenticationDao mutableAuthenticationDao = null; // TODO: (MutableAuthenticationDao) factory.getBean("authenticationDao");

        // Clean up invitee's user account and person node if they are not in use i.e.
        // account is still disabled and there are no pending invites outstanding for the
        // invitee
        InviteHelper.cleanUpStaleInviteeResources(inviteeUserName, mutableAuthenticationDao, personService,
                workflowService);
    }
}
