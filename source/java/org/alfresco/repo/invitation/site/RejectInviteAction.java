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
package org.alfresco.repo.invitation.site;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets completed
 * along the "reject" transition
 * 
 * @author glen johnson at alfresco com
 */
public class RejectInviteAction extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 4377660284993206875L;
    
    private MutableAuthenticationDao mutableAuthenticationDao;
    private PersonService personService;
    private WorkflowService workflowService;

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
    }

    /* (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        // get the invitee user name
        final String inviteeUserName = (String) executionContext.getVariable(WorkflowModelNominatedInvitation.wfVarInviteeUserName);

        // clean up invitee's user account and person node if they are not in use i.e.
        // account is still disabled and there are no pending invites outstanding for the
        // invitee
        InviteHelper.cleanUpStaleInviteeResources(inviteeUserName, mutableAuthenticationDao, personService,
                workflowService);
    }
}
