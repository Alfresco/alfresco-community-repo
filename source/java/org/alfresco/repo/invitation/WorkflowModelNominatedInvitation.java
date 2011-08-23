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

import org.alfresco.service.namespace.QName;

/**
 * Workflow Model for a Nominated Invitation
 */
public interface WorkflowModelNominatedInvitation
{
    // namespace
    public static final String NAMESPACE_URI = "http://www.alfresco.org/model/workflow/invite/nominated/1.0";

    // process name
    public static final QName WF_PROCESS_INVITE = QName.createQName(NAMESPACE_URI, "invitation-nominated");

    // workflow definition name
    public static final String WORKFLOW_DEFINITION_NAME = "jbpm$inwf:invitation-nominated";
    public static final String WORKFLOW_DEFINITION_NAME_ACTIVITI = "activiti$activitiInvitationNominated";

    // tasks
    public static final QName WF_TASK_INVITE_TO_SITE = QName.createQName(NAMESPACE_URI, "inviteToSiteTask");
    public static final QName WF_TASK_INVITE_PENDING = QName.createQName(NAMESPACE_URI, "invitePendingTask");
    public static final QName WF_TASK_ACTIVIT_INVITE_PENDING = QName.createQName(NAMESPACE_URI, "activitiInvitePendingTask");
    public static final QName WF_TASK_ACCEPT_INVITE = QName.createQName(NAMESPACE_URI, "acceptInviteTask");
    public static final QName WF_TASK_REJECT_INVITE = QName.createQName(NAMESPACE_URI, "rejectInviteTask");

    // transition names
    public static final String WF_TRANSITION_SEND_INVITE = "sendInvite";
    public static final String WF_TRANSITION_ACCEPT = "accept";
    public static final String WF_TRANSITION_REJECT = "reject";
    public static final String WF_TRANSITION_CANCEL = "cancel";
    public static final String WF_TRANSITION_ACCEPT_INVITE_END = "end";
    public static final String WF_TRANSITION_REJECT_INVITE_END = "end";

    // workflow properties
    public static final QName WF_PROP_SERVER_PATH = QName.createQName(NAMESPACE_URI, "serverPath");
    public static final QName WF_PROP_ACCEPT_URL = QName.createQName(NAMESPACE_URI, "acceptUrl");
    public static final QName WF_PROP_REJECT_URL = QName.createQName(NAMESPACE_URI, "rejectUrl");
    public static final QName WF_PROP_INVITE_TICKET = QName.createQName(NAMESPACE_URI, "inviteTicket");
    public static final QName WF_PROP_INVITER_USER_NAME = QName.createQName(NAMESPACE_URI, "inviterUserName");
    public static final QName WF_PROP_INVITEE_USER_NAME = QName.createQName(NAMESPACE_URI, "inviteeUserName");
    public static final QName WF_PROP_INVITEE_EMAIL = QName.createQName(NAMESPACE_URI, "inviteeEmail");
    public static final QName WF_PROP_INVITEE_FIRSTNAME = QName.createQName(NAMESPACE_URI, "inviteeFirstName");
    public static final QName WF_PROP_INVITEE_LASTNAME = QName.createQName(NAMESPACE_URI, "inviteeLastName");
    public static final QName WF_PROP_RESOURCE_TYPE = QName.createQName(NAMESPACE_URI, "resourceType");
    public static final QName WF_PROP_RESOURCE_NAME = QName.createQName(NAMESPACE_URI, "resourceName");
    public static final QName WF_PROP_RESOURCE_TITLE = QName.createQName(NAMESPACE_URI, "resourceTitle");
    public static final QName WF_PROP_RESOURCE_DESCRIPTION = QName.createQName(NAMESPACE_URI, "resourceDescription");   
    public static final QName WF_PROP_INVITEE_ROLE = QName.createQName(NAMESPACE_URI, "inviteeRole");
    public static final QName WF_PROP_INVITEE_GEN_PASSWORD = QName.createQName(NAMESPACE_URI, "inviteeGenPassword");

    // workflow execution context variable names
    public static final String wfVarInviteeUserName = "inwf_inviteeUserName";
    public static final String wfVarInviterUserName = "inwf_inviterUserName";
    public static final String wfVarResourceName = "inwf_resourceName";
    public static final String wfVarResourceTitle = "inwf_resourceTitle";
    public static final String wfVarResourceDescription = "inwf_resourceDescription";
    public static final String wfVarResourceType = "inwf_resourceType";
    public static final String wfVarWorkflowInstanceId = "workflowinstanceid";
    public static final String wfVarRole = "inwf_inviteeRole";
    public static final String wfVarInviteTicket = "inwf_inviteTicket";
    public static final String wfVarServerPath = "inwf_serverPath";
    public static final String wfVarAcceptUrl = "inwf_acceptUrl";
    public static final String wfVarRejectUrl = "inwf_rejectUrl";
    public static final String wfVarInviteeGenPassword = "inwf_inviteeGenPassword";

}
