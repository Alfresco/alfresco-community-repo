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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.site.SiteService;

/**
 * Activiti delegate that is executed when a invitation request has
 * been accepted.
 *
 * @author Frederik Heremans
 */
public class AcceptInviteDelegate extends BaseJavaDelegate
{
    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        final ServiceRegistry serviceRegistry = getServiceRegistry();
        final MutableAuthenticationDao mutableAuthenticationDao = null; // TODO: (MutableAuthenticationDao) factory.getBean("authenticationDao");
        final SiteService siteService = serviceRegistry.getSiteService();
        
        final String inviteeUserName = (String) execution.getVariable(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        final String siteShortName = (String) execution.getVariable(WorkflowModelNominatedInvitation.wfVarResourceName);
        final String inviterUserName = (String) execution.getVariable(WorkflowModelNominatedInvitation.wfVarInviterUserName);
        final String inviteeSiteRole = (String) execution.getVariable(WorkflowModelNominatedInvitation.wfVarRole);
        
        // If there is already a user account for the invitee and that account
        // is disabled, then enable the account because he/she has accepted the
        // site invitation
        if ((mutableAuthenticationDao.userExists(inviteeUserName))
            && !(mutableAuthenticationDao.getEnabled(inviteeUserName)))
        {
            mutableAuthenticationDao.setEnabled(inviteeUserName, true);
        }

        // Add Invitee to Site with the site role that the inviter "started" the invite process with
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                siteService.setMembership(siteShortName,
                        inviteeUserName, inviteeSiteRole);

                return null;
            }
            
        }, inviterUserName);
    }
}
