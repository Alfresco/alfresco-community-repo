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


import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * JBPM Action fired when a moderated invitation is rejected.
 */
public class ModeratedActionReject extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 4377660284993206875L;
    private static final Log logger = LogFactory.getLog(ModeratedActionReject.class);
    
    private ActionService actionService;
    private TemplateService templateService;
    //private String rejectTemplate = " PATH:\"app:company_home/app:dictionary/app:email_templates/cm:invite/cm:moderated-reject-email.ftl\"";
    private String rejectTemplate = "/alfresco/bootstrap/invite/moderated-reject-email.ftl";

    private boolean sendEmails = true;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        ServiceRegistry services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        templateService = services.getTemplateService();
        actionService = services.getActionService();
        sendEmails = services.getInvitationService().isSendEmails();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        //Do nothing if emails disabled.
        if(sendEmails == false)
        {
            return;
        }
        
        final String resourceType = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarResourceType);
        final String resourceName = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarResourceName);
        final String inviteeUserName = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        final String inviteeRole = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        final String reviewer = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarReviewer);
        final String reviewComments = (String)executionContext.getVariable(WorkflowModelModeratedInvitation.wfVarReviewComments);
        
        // send email to the invitee if possible - but don't fail the rejection if email cannot be sent
        try 
        {
        	Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
        	model.put("resourceName", resourceName);
        	model.put("resourceType", resourceType);
        	model.put("inviteeRole", inviteeRole);
        	model.put("reviewComments", reviewComments);
        	model.put("reviewer", reviewer);
        	model.put("inviteeUserName", inviteeUserName);
     
        	String emailMsg = templateService.processTemplate("freemarker", rejectTemplate,  model);
        	        
        	Action emailAction = actionService.createAction("mail");
        	emailAction.setParameterValue(MailActionExecuter.PARAM_TO, inviteeUserName);
        	emailAction.setParameterValue(MailActionExecuter.PARAM_FROM, reviewer);
        	emailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Rejected invitation to web site:" + resourceName);
        	emailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, emailMsg);
        	emailAction.setExecuteAsynchronously(true);
        	actionService.executeAction(emailAction, null);
        }
        catch(Exception e)
        {
        	// Swallow exception
        	logger.error("unable to send reject email", e);
        }
    }
}