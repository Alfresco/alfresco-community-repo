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

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets cancelled
 * along the "cancel" transition
 * 
 * @author glen johnson at alfresco com
 */
public class CancelInviteAction extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 776961141883350908L;
    
    private MutableAuthenticationDao mutableAuthenticationDao;
    private PersonService personService;
    private WorkflowService workflowService;
    private SiteService siteService;

    private final String MSG_NOT_SITE_MANAGER = "invitation.cancel.not_site_manager";
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        ServiceRegistry services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        mutableAuthenticationDao = (MutableAuthenticationDao) factory.getBean("authenticationDao");
        personService = services.getPersonService();
        workflowService = services.getWorkflowService();
        siteService = services.getSiteService();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        // get the invitee user name and site short name variables off the execution context
        final String inviteeUserName = (String) executionContext.getVariable(
                WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        final String siteShortName = (String) executionContext.getVariable(
                WorkflowModelNominatedInvitation.wfVarResourceName);
        final String inviteId = (String) executionContext.getVariable(
                WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId);
        
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        String currentUserSiteRole = this.siteService.getMembersRole(siteShortName, currentUserName);
        if ((currentUserSiteRole == null) || (currentUserSiteRole.equals(SiteModel.SITE_MANAGER) == false))
        {
        	// The current user is not the site manager
        	Object[] args = {currentUserName, inviteId, siteShortName};
            throw new InvitationExceptionForbidden(MSG_NOT_SITE_MANAGER, args);
        }
        
        // clean up invitee's user account and person node if they are not in use i.e.
        // account is still disabled and there are no pending invites outstanding for the
        // invitee
        InviteHelper.cleanUpStaleInviteeResources(inviteeUserName, mutableAuthenticationDao, personService,
                workflowService);
    }
}
