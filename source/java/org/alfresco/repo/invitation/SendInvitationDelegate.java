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

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarAcceptUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteTicket;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeGenPassword;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviterUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRejectUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRole;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarServerPath;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.invitation.site.InviteSender;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Activiti delegate that is executed when a invitation request has
 * been sent.
 *
 * @author Frederik Heremans
 */
public class SendInvitationDelegate extends BaseJavaDelegate
{

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        ServiceRegistry serviceRegistry = getServiceRegistry();
        
        if(serviceRegistry.getInvitationService().isSendEmails())
        {
            // TODO: Get hold of beans
            // Repository repository = (Repository) factory.getBean("repositoryHelper");
            // MessageService messageService = (MessageService) factory.getBean("messageService");
            NamespaceService namespaceService = serviceRegistry.getNamespaceService();
            
            // TODO: revive, once dependencies can be obtained
            // InviteSender inviteSender = new InviteSender(serviceRegistry, repository, messageService);
            
            Collection<String> propertyNames = Arrays.asList(wfVarInviteeUserName,//
                        wfVarResourceName,//
                        wfVarInviterUserName,//
                        wfVarInviteeUserName,//
                        wfVarRole,//
                        wfVarInviteeGenPassword,//
                        wfVarResourceName,//
                        wfVarInviteTicket,//
                        wfVarServerPath,//
                        wfVarAcceptUrl,//
                        wfVarRejectUrl,
                        InviteSender.WF_INSTANCE_ID);
            Map<String, String> properties = makePropertiesFromContext(execution, propertyNames);
    
            String packageName = WorkflowModel.ASSOC_PACKAGE.toPrefixString(namespaceService).replace(":", "_");
            ScriptNode packageNode = (ScriptNode) execution.getVariable(packageName);
            String packageRef = packageNode.getNodeRef().toString();
            properties.put(InviteSender.WF_PACKAGE, packageRef);
            
            String instanceName=WorkflowModel.PROP_WORKFLOW_INSTANCE_ID.toPrefixString(namespaceService).replace(":", "_");
            String instanceId = (String) execution.getVariable(instanceName);
            properties.put(InviteSender.WF_INSTANCE_ID, instanceId);
            
            // TODO: revive, once dependencies can be obtained
            //inviteSender.sendMail(properties);
        }
    }
    
    private Map<String, String> makePropertiesFromContext(DelegateExecution execution, Collection<String> propertyNames)
    {
        Map<String, String> props = new HashMap<String, String>();
        for (String name : propertyNames)
        {
            String value = (String) execution.getVariable(name);
            props.put(name, value);
        }
        return props;
    }

}
