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
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.site.SiteService;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets completed
 * along the "accept" transition
 * 
 * @author glen johnson at alfresco com
 */
public class AcceptInviteAction extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 8133039174866049136L;

    private SiteService siteService;
    private MutableAuthenticationDao mutableAuthenticationDao;

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        ServiceRegistry services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        mutableAuthenticationDao = (MutableAuthenticationDao) factory.getBean("authenticationDao");
        siteService = services.getSiteService();
    }

    /* (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        final String inviteeUserName = (String) executionContext.getVariable(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        final String siteShortName = (String) executionContext.getVariable(WorkflowModelNominatedInvitation.wfVarResourceName);
        final String inviterUserName = (String) executionContext.getVariable(WorkflowModelNominatedInvitation.wfVarInviterUserName);
        final String inviteeSiteRole = (String) executionContext.getVariable(WorkflowModelNominatedInvitation.wfVarRole);
        
        // if there is already a user account for the invitee and that account
        // is disabled, then enable the account because he/she has accepted the
        // site invitation
        if ((this.mutableAuthenticationDao.userExists(inviteeUserName))
            && (this.mutableAuthenticationDao.getEnabled(inviteeUserName) == false))
        {
            this.mutableAuthenticationDao.setEnabled(inviteeUserName, true);
        }

        // add Invitee to Site with the site role that the inviter "started" the invite process with
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                AcceptInviteAction.this.siteService.setMembership(siteShortName,
                        inviteeUserName, inviteeSiteRole);

                return null;
            }
            
        }, inviterUserName);
    }
}
