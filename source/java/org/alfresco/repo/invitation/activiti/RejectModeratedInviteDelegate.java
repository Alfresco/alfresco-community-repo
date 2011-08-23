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
package org.alfresco.repo.invitation.activiti;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.invitation.ModeratedActionReject;
import org.alfresco.repo.invitation.WorkflowModelModeratedInvitation;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Activiti delegate that is executed when a invitation-moderated process is reviewed 
 * and rejected. 
 * 
 * <b>Same behaviour as {@link ModeratedActionReject}</b>
 *
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 4.0
 */
public class RejectModeratedInviteDelegate extends AbstractInvitationDelegate
{
    private static final String REJECT_TEMPLATE = "/alfresco/bootstrap/invite/moderated-reject-email.ftl";
    private static final Log logger = LogFactory.getLog(RejectModeratedInviteDelegate.class);
    
    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        final ServiceRegistry serviceRegistry = getServiceRegistry();
        
        // Do nothing if emails disabled.
        if(!serviceRegistry.getInvitationService().isSendEmails())
        {
            return;
        }
        
        final String resourceType = (String) execution.getVariable(WorkflowModelModeratedInvitation.wfVarResourceType);
        final String resourceName = (String) execution.getVariable(WorkflowModelModeratedInvitation.wfVarResourceName);
        final String inviteeUserName = (String) execution.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        final String inviteeRole = (String) execution.getVariable(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        final String reviewer = (String) execution.getVariable(WorkflowModelModeratedInvitation.wfVarReviewer);
        final String reviewComments = (String) execution.getVariable(WorkflowModelModeratedInvitation.wfVarReviewComments);
        
        final TemplateService templateService = serviceRegistry.getTemplateService();
        final ActionService actionService = serviceRegistry.getActionService();
        
        // send email to the invitee if possible - but don't fail the rejection if email cannot be sent
        try 
        {
            // Build our model
            Map<String, Serializable> model = new HashMap<String, Serializable>(8, 1.0f);
            model.put("resourceName", resourceName);
            model.put("resourceType", resourceType);
            model.put("inviteeRole", inviteeRole);
            model.put("reviewComments", reviewComments);
            model.put("reviewer", reviewer);
            model.put("inviteeUserName", inviteeUserName);
            
            // Process the template
            // Note - because we use a classpath template, rather than a Data Dictionary
            //        one, we can't have the MailActionExecutor do the template for us
            String emailMsg = templateService.processTemplate("freemarker", REJECT_TEMPLATE,  model);
                    
            // Send
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
