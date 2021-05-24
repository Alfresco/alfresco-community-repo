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

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarAcceptUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteTicket;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeGenPassword;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviterUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRejectUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRole;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarServerPath;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.activiti.SendNominatedInviteDelegate;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.util.ModelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * This class is responsible for sending email invitations, allowing nominated
 * user's to join a Site.
 * 
 * @author Nick Smith
 */
public class InviteNominatedSender extends InviteSender
{
    private static final Log logger = LogFactory.getLog(InviteNominatedSender.class);
    public static final String WF_INSTANCE_ID = "wf_instanceId";
    public static final String WF_PACKAGE = "wf_package";
    public static final String SITE_LEAVE_HASH = "#leavesite";
    private static final String SITE_DASHBOARD_ENDPOINT_PATTERN =  "/page/site/{0}/dashboard";

    private static final List<String> INVITE_NOMINATED_EXPECTED_PROPERTIES = Arrays.asList(wfVarInviteeUserName,//
                wfVarResourceName,//
                wfVarInviterUserName,//
                wfVarInviteeUserName,//
                wfVarRole,//
                wfVarInviteeGenPassword,//
                wfVarResourceName,//
                wfVarInviteTicket,//
                wfVarServerPath,//
                wfVarAcceptUrl,//
                wfVarRejectUrl, WF_INSTANCE_ID,//
                WF_PACKAGE);
    
    public InviteNominatedSender(ServiceRegistry services, Repository repository, MessageService messageService)
    {
        super(services, repository, messageService);
    }

    /**
     * Implemented for backwards compatibility
     * 
     * @param properties
     * @deprecated
     * @see {@link #sendMail(String, String, Map)}
     */
    public void sendMail(Map<String, String> properties)
    {
        sendMail(SendNominatedInviteDelegate.EMAIL_TEMPLATE_XPATH, SendNominatedInviteDelegate.EMAIL_SUBJECT_KEY, properties);
    }

    @Override
    public void sendMail(String emailTemplateXpath, String emailSubjectKey, Map<String, String> properties)
    {
        checkProperties(properties);
        ParameterCheck.mandatory("Properties", properties);
        NodeRef inviter = personService.getPerson(properties.get(wfVarInviterUserName));
        String inviteeName = properties.get(wfVarInviteeUserName);
        NodeRef invitee = personService.getPerson(inviteeName);
        Action mail = actionService.createAction(MailActionExecuter.NAME);
        mail.setParameterValue(MailActionExecuter.PARAM_FROM, getEmail(inviter));
        String recipient = getEmail(invitee);
        if(StringUtils.isEmpty(recipient))
        {
            logger.warn("Cannot send invitation: Invitee user account does not have email");
            return;
        }
        mail.setParameterValue(MailActionExecuter.PARAM_TO, recipient);
        mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT, emailSubjectKey);
        mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT_PARAMS, new Object[] { ModelUtil.getProductName(repoAdminService), getSiteName(properties) });
        mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, getEmailTemplateNodeRef(emailTemplateXpath));
        mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) buildMailTextModel(properties));
        mail.setParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, true);
        actionService.executeAction(mail, getWorkflowPackage(properties));
    }

    @Override
    protected Map<String, Serializable> buildMailTextModel(Map<String, String> properties)
    {
        NodeRef inviter = personService.getPerson(properties.get(wfVarInviterUserName));
        NodeRef invitee = personService.getPerson(properties.get(wfVarInviteeUserName));
        // Set the core model parts
        // Note - the user part is skipped, as that's implied via the run-as
        Map<String, Serializable> model = new HashMap<String, Serializable>();
        model.put(TemplateService.KEY_COMPANY_HOME, repository.getCompanyHome());
        model.put(TemplateService.KEY_USER_HOME, repository.getUserHome(repository.getPerson()));
        model.put(TemplateService.KEY_PRODUCT_NAME, ModelUtil.getProductName(repoAdminService));

        // Build up the args for rendering inside the template
        Map<String, String> args = buildArgs(properties, inviter, invitee);
        model.put("args", (Serializable) args);

        // All done
        return model;
    }

    private Map<String, String> buildArgs(Map<String, String> properties, NodeRef inviter, NodeRef invitee)
    {
        String params = buildUrlParamString(properties);
        String acceptLink = makeLink(properties.get(wfVarServerPath), properties.get(wfVarAcceptUrl), params, null);
        String rejectLink = makeLink(properties.get(wfVarServerPath), properties.get(wfVarRejectUrl), params, null);

        String siteDashboardEndpoint = getSiteDashboardEndpoint(properties);
        String siteDashboardLink = makeLink(properties.get(wfVarServerPath), siteDashboardEndpoint, null, null);
        String siteLeaveLink = makeLink(properties.get(wfVarServerPath), siteDashboardEndpoint, null, SITE_LEAVE_HASH);

        Map<String, String> args = new HashMap<String, String>();
        args.put("inviteePersonRef", invitee.toString());
        args.put("inviterPersonRef", inviter.toString());
        args.put("siteName", getSiteName(properties));
        args.put("inviteeSiteRole", getRoleName(properties));
        args.put("inviteeUserName", properties.get(wfVarInviteeUserName));
        args.put("inviteeGenPassword", properties.get(wfVarInviteeGenPassword));
        args.put("acceptLink", acceptLink);
        args.put("rejectLink", rejectLink);
        args.put("siteDashboardLink", siteDashboardLink);
        args.put("siteLeaveLink", siteLeaveLink);
        return args;
    }

    protected String makeLink(String location, String endpoint, String queryParams, String hashParam)
    {
        location = location.endsWith("/") ? location : location + "/";
        endpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
        if (queryParams != null)
        {
            queryParams = queryParams.startsWith("?") ? queryParams : "?" + queryParams;
        }
        else
        {
            queryParams = "";
        }
        if (hashParam != null)
        {
            hashParam = hashParam.startsWith("#") ? hashParam : "#" + hashParam;
        }
        else
        {
            hashParam = "";
        }
        return location + endpoint + queryParams + hashParam;
    }

    private String getRoleName(Map<String, String> properties)
    {
        String roleName = properties.get(wfVarRole);
        String role = messageService.getMessage("invitation.invitesender.email.role." + roleName);
        if (role == null)
        {
            role = roleName;
        }
        return role;
    }

    private String getEmail(NodeRef person)
    {
        return (String) nodeService.getProperty(person, ContentModel.PROP_EMAIL);
    }

    private NodeRef getWorkflowPackage(Map<String, String> properties)
    {
        String packageRef = properties.get(WF_PACKAGE);
        return new NodeRef(packageRef);
    }

    private String buildUrlParamString(Map<String, String> properties)
    {
        StringBuilder params = new StringBuilder("?inviteId=");
        params.append(properties.get(WF_INSTANCE_ID));
        params.append("&inviteeUserName=");
        params.append(URLEncoder.encode(properties.get(wfVarInviteeUserName)));
        params.append("&siteShortName=");
        params.append(properties.get(wfVarResourceName));
        params.append("&inviteTicket=");
        params.append(properties.get(wfVarInviteTicket));
        return params.toString();
    }

    private String getSiteDashboardEndpoint(Map<String, String> properties)
    {
        String siteName = properties.get(wfVarResourceName);
        return MessageFormat.format(SITE_DASHBOARD_ENDPOINT_PATTERN, siteName);
    }

    @Override
    public List<String> getRequiredProperties()
    {
        return INVITE_NOMINATED_EXPECTED_PROPERTIES;
    }

    @Override
    protected String getWorkflowPropForSiteName()
    {
        return wfVarResourceName;
    }
}
