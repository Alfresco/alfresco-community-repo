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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.invite;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteService;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
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

    /**
     * Inner class providing functionality (which needs to run under admin
     * rights) to set membership of invitee (given as invitee user name) to site
     * (given as site short name) as given site role
     */
    private class SetSiteMembershipWorker implements
            AuthenticationUtil.RunAsWork<Boolean>
    {
        private String siteShortName;
        private String inviteeUserName;
        private String siteRole;

        private SetSiteMembershipWorker(String siteShortName,
                String inviteeUserName, String siteRole)
        {
            this.siteShortName = siteShortName;
            this.inviteeUserName = inviteeUserName;
            this.siteRole = siteRole;
        }

        /**
         * Does the work to set the site membership
         */
        public Boolean doWork() throws Exception
        {
            AcceptInviteAction.this.siteService.setMembership(this.siteShortName,
                    this.inviteeUserName, this.siteRole);

            return Boolean.TRUE;
        }
    }

    private static final String USER_ADMIN = "admin";
    
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
        String inviteeUserName = (String) executionContext.getVariable("wf_inviteeUserName");
        String siteShortName = (String) executionContext.getVariable("wf_siteShortName");
        String inviteeSiteRole = (String) executionContext.getVariable("wf_inviteeSiteRole");
        
        // if there is already a user account for the invitee and that account
        // is disabled, then enable the account because he/she has accepted the
        // site invitation
        if ((this.mutableAuthenticationDao.userExists(inviteeUserName))
            && (this.mutableAuthenticationDao.getEnabled(inviteeUserName) == false))
        {
            this.mutableAuthenticationDao.setEnabled(inviteeUserName, true);
        }

        // add Invitee to Site with the site role that the inviter "started" the invite process with
        RunAsWork<Boolean> setSiteMembershipWorker = new SetSiteMembershipWorker(
                siteShortName, inviteeUserName, inviteeSiteRole);
        AuthenticationUtil.runAs(setSiteMembershipWorker, USER_ADMIN);
    }
}
