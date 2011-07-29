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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;

/**
 * Activiti delegate that is executed when a invitation request has
 * been cancelled.
 *
 * @author Frederik Heremans
 */
public class CancelInviteDelegate extends BaseJavaDelegate
{
    private final String MSG_NOT_SITE_MANAGER = "invitation.cancel.not_site_manager";
    
    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        // Get the invitee user name and site short name variables off the execution context
        final String inviteeUserName = (String) execution.getVariable(
                WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        final String siteShortName = (String) execution.getVariable(
                WorkflowModelNominatedInvitation.wfVarResourceName);
        final String inviteId = (String) execution.getVariable(
                WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId);
        
        ServiceRegistry serviceRegistry = getServiceRegistry();
        WorkflowService workflowService = serviceRegistry.getWorkflowService();
        PersonService personService = serviceRegistry.getPersonService();
        SiteService siteService = serviceRegistry.getSiteService();
        MutableAuthenticationDao mutableAuthenticationDao = null; // TODO: (MutableAuthenticationDao) factory.getBean("authenticationDao");
        
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        String currentUserSiteRole = siteService.getMembersRole(siteShortName, currentUserName);
        if ((currentUserSiteRole == null) || (currentUserSiteRole.equals(SiteModel.SITE_MANAGER) == false))
        {
            // The current user is not the site manager
            Object[] args = {currentUserName, inviteId, siteShortName};
            throw new InvitationExceptionForbidden(MSG_NOT_SITE_MANAGER, args);
        }
        
        // Clean up invitee's user account and person node if they are not in use i.e.
        // account is still disabled and there are no pending invites outstanding for the
        // invitee
        InviteHelper.cleanUpStaleInviteeResources(inviteeUserName, mutableAuthenticationDao, personService,
                workflowService);

    }
}
