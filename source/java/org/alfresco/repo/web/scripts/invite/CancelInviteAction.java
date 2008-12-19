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
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteService;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
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

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        ServiceRegistry services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        mutableAuthenticationDao = (MutableAuthenticationDao) factory.getBean("authenticationDao");
        personService = (PersonService) services.getPersonService();
        workflowService = (WorkflowService) services.getWorkflowService();
        siteService = (SiteService) services.getSiteService();
    }

    /* (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        // get the invitee user name and site short name variables off the execution context
        final String inviteeUserName = (String) executionContext.getVariable(
                InviteWorkflowModel.wfVarInviteeUserName);
        final String siteShortName = (String) executionContext.getVariable(
                InviteWorkflowModel.wfVarSiteShortName);
        final String inviteId = (String) executionContext.getVariable(
                InviteWorkflowModel.wfVarWorkflowInstanceId);
        
        // throw http status 'forbidden' Web Script Exception if current user is not a Site Manager of the site
        // associated with the invite (identified by inviteID)
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        String currentUserSiteRole = this.siteService.getMembersRole(siteShortName, currentUserName);
        if ((currentUserSiteRole == null) || (currentUserSiteRole.equals(SiteModel.SITE_MANAGER) == false))
        {
            throw new WebScriptException(Status.STATUS_FORBIDDEN,
                    "Current user '" + currentUserName + "' cannot cancel invite having ID '" + inviteId
                    + "' because he\\she is not a Site Manager of the site with short name:'" + siteShortName
                    + "'");
        }
        
        // clean up invitee's user account and person node if they are not in use i.e.
        // account is still disabled and there are no pending invites outstanding for the
        // invitee
        InviteHelper.cleanUpStaleInviteeResources(inviteeUserName, mutableAuthenticationDao, personService,
                workflowService);
    }
}
