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
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.site.SiteService;

/**
 * Activiti delegate that is executed when a invitation-moderated process is reviewed 
 * and approved.
 *
 * @author Frederik Heremans
 */
public class ModerationApproveDelegate extends BaseJavaDelegate
{

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        final String resourceName = (String)execution.getVariable(WorkflowModelModeratedInvitation.wfVarResourceName);
        final String inviteeUserName = (String)execution.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        final String inviteeRole = (String)execution.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        final String reviewer = (String)execution.getVariable(WorkflowModelModeratedInvitation.wfVarReviewer);
        
        final SiteService siteService = getServiceRegistry().getSiteService();
        
        // Add invitee to the site
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // Add the new user to the web site
                siteService.setMembership(resourceName, inviteeUserName, inviteeRole);
                return null;
            }
            
        }, reviewer);
    }

}
