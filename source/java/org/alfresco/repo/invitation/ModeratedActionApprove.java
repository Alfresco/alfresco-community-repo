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


import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.site.SiteService;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 *
 */
public class ModeratedActionApprove extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 4377660284993206875L;
    
    private SiteService siteService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        ServiceRegistry services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        siteService = services.getSiteService();
    }

    /**
     * {@inheritDoc}
     **/
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        final String resourceName = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarResourceName);
        final String inviteeUserName = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        final String inviteeRole = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        final String reviewer = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarReviewer);
    	
        /**
         * Add invitee to the site
         */
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