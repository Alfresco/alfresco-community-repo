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

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.namespace.NamespaceService;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

public class SendInviteAction extends JBPMSpringActionHandler
{
    // TODO Select Version Id.
    private static final long serialVersionUID = 8133039174866049136L;

    private InviteSender inviteSender;
    private NamespaceService namespaceService;

    private boolean sendEmails;

    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        Repository repository = (Repository) factory.getBean("repositoryHelper");
        ServiceRegistry services = (ServiceRegistry) factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        MessageService messageService= (MessageService) factory.getBean("messageService");
        inviteSender = new InviteSender(services, repository, messageService);
        namespaceService = services.getNamespaceService();
        InvitationService invitationService = services.getInvitationService();
        sendEmails = invitationService.isSendEmails();
    }

    public void execute(final ExecutionContext context) throws Exception
    {
        if(sendEmails == false)
            return;
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
        Map<String, String> properties = makePropertiesFromContext(context, propertyNames);

        String packageName = WorkflowModel.ASSOC_PACKAGE.toPrefixString(namespaceService).replace(":", "_");
        ScriptNode packageNode = (ScriptNode) context.getVariable(packageName);
        String packageRef = packageNode.getNodeRef().toString();
        properties.put(InviteSender.WF_PACKAGE, packageRef);
        
        String instanceName=WorkflowModel.PROP_WORKFLOW_INSTANCE_ID.toPrefixString(namespaceService).replace(":", "_");
        String instanceId = (String) context.getVariable(instanceName);
        properties.put(InviteSender.WF_INSTANCE_ID, instanceId);
        inviteSender.sendMail(properties);
    }

    private Map<String, String> makePropertiesFromContext(ExecutionContext context, Collection<String> propertyNames)
    {
        Map<String, String> props = new HashMap<String, String>();
        for (String name : propertyNames)
        {
            String value = (String) context.getVariable(name);
            props.put(name, value);
        }
        return props;
    }
}
