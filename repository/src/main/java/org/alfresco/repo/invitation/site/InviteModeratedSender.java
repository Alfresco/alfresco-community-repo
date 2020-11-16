/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.invitation.site;

import static org.alfresco.repo.invitation.WorkflowModelModeratedInvitation.bpmGroupAssignee;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.WorkflowModelModeratedInvitation;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;


/**
 * This class is responsible for sending email notifications to site managers informing about users requesting access.
 * 
 * @author Constantin Popa
 */
public class InviteModeratedSender extends InviteSender
{    
    private static final String DATA_DICTIONARY_XPATH_PREFIX = "app:";
    public static final String WF_PACKAGE = "wf_package";
    public static final String SHARE_PENDING_INVITES_LINK = "{0}/page/site/{1}/pending-invites";
    public static final String WORKSPACE_PENDING_INVITES_LINK = "{0}/#/{1}/members/libraries";

    private static final List<String> INVITE_MODERATED_EXPECTED_PROPERTIES = Arrays.asList(
            WorkflowModelModeratedInvitation.wfVarInviteeUserName,
            WorkflowModelModeratedInvitation.wfVarInviteeRole,
            WorkflowModelModeratedInvitation.wfVarResourceName,
            WorkflowModelModeratedInvitation.bpmGroupAssignee,
            WorkflowModelModeratedInvitation.wfVarResourceType);

    
    public InviteModeratedSender(ServiceRegistry services, Repository repository, MessageService messageService)
    {
        super(services, repository, messageService);
    }

    @Override
    public void sendMail(String emailTemplatePath, String emailSubjectKey, Map<String, String> properties)
    {
        checkProperties(properties);
        NodeRef invitee = personService.getPerson(properties.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName));
        Action mail = actionService.createAction(MailActionExecuter.NAME);
        mail.setParameterValue(MailActionExecuter.PARAM_FROM, (String) nodeService.getProperty(invitee, ContentModel.PROP_EMAIL));
        mail.setParameterValue(MailActionExecuter.PARAM_TO_MANY, properties.get(bpmGroupAssignee));
        mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT, emailSubjectKey);
        mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT_PARAMS, new Object[] { getSiteName(properties) });
        mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, getEmailTemplate(emailTemplatePath));
        mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) buildMailTextModel(properties));
        mail.setParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, true);
        actionService.executeAction(mail, new NodeRef(properties.get(WF_PACKAGE)));
    }
    
    private Serializable getEmailTemplate(String emailTemplatePath){
        return emailTemplatePath.startsWith(DATA_DICTIONARY_XPATH_PREFIX)? getEmailTemplateNodeRef(emailTemplatePath) : emailTemplatePath;
    }

    @Override
    protected Map<String, Serializable> buildMailTextModel(Map<String, String> properties)
    {
        NodeRef invitee = personService.getPerson(properties.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName));
        Map<String, Serializable> model = new HashMap<String, Serializable>();
        model.put(TemplateService.KEY_COMPANY_HOME, repository.getCompanyHome());
        model.put(TemplateService.KEY_USER_HOME, repository.getUserHome(repository.getPerson()));
        model.put(TemplateService.KEY_PRODUCT_NAME, ModelUtil.getProductName(repoAdminService));

        PersonInfo inviteePerson = personService.getPerson(invitee);
        model.put("inviteeName", StringUtils.join(new String[] { inviteePerson.getFirstName(), inviteePerson.getLastName() }, " "));
        model.put("siteName", getSiteName(properties));
        model.put("sharePendingInvitesLink", StringUtils.stripStart(getPendingInvitesLink(properties), "/"));
        model.put("workspacePendingInvitesLink", StringUtils.stripStart(getWorkSpaceInvitesLink(properties), "/"));
        model.put("template_assets_url", getTemplateAssetsLink(properties));
        return model;
    }

    protected String getPendingInvitesLink(Map<String, String> properties)
    {
        return MessageFormat.format(SHARE_PENDING_INVITES_LINK, TenantUtil.getCurrentDomain(),
                                    properties.get(WorkflowModelModeratedInvitation.wfVarResourceName));
    }

    protected String getWorkSpaceInvitesLink(Map<String, String> properties)
    {
        String path = properties.get(WorkflowModelModeratedInvitation.wfVarWorkspaceUrl);
        boolean hasValidBaseUrl =  path != null && !StringUtils.isAllBlank(path) && path.length() > 1;
        String workspaceUrl = sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort()
                + ( hasValidBaseUrl ?   "/" + path.trim() : "");

        return MessageFormat.format(WORKSPACE_PENDING_INVITES_LINK, workspaceUrl, properties.get(WorkflowModelModeratedInvitation.wfVarResourceName));
    }

    protected String getTemplateAssetsLink(Map<String, String> properties)
    {
        if (properties.get(WorkflowModelModeratedInvitation.wfVarTemplateAssetsUrl) != null) {
            return UrlUtil.replaceAlfrescoUrlPlaceholder(properties.get(WorkflowModelModeratedInvitation.wfVarTemplateAssetsUrl), this.sysAdminParams);
        }
        return UrlUtil.getAlfrescoUrl(this.sysAdminParams);
    }

    @Override
    public List<String> getRequiredProperties()
    {
        return INVITE_MODERATED_EXPECTED_PROPERTIES;
    }

    @Override
    protected String getWorkflowPropForSiteName()
    {
        return WorkflowModelModeratedInvitation.wfVarResourceName;
    }
}
