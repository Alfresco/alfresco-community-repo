package org.alfresco.repo.web.scripts.invite;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public interface InviteWorkflowModel {

    // process name
    public static final QName WF_PROCESS_INVITE = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "invite");

    // workflow definition name
    public static final String WORKFLOW_DEFINITION_NAME = "jbpm$wf:invite";
    
    // tasks
    public static final QName WF_INVITE_TASK_INVITE_TO_SITE = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteToSiteTask");
    public static final QName WF_TASK_ACCEPT_INVITE = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "acceptInviteTask");
    public static final QName WF_TASK_REJECT_INVITE = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "rejectInviteTask");
    public static final QName WF_TASK_INVITE_PENDING = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "invitePendingTask");
    
    
    // transition names
    public static final String WF_TRANSITION_SEND_INVITE = "sendInvite";
    public static final String WF_TRANSITION_ACCEPT = "accept";
    public static final String WF_TRANSITION_REJECT = "reject";
    public static final String WF_TRANSITION_ACCEPT_INVITE_END = "end";
    public static final String WF_TRANSITION_REJECT_INVITE_END = "end";
    
    // workflow properties
    public static final QName WF_PROP_SERVER_PATH = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "serverPath");
    public static final QName WF_PROP_ACCEPT_URL = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "acceptUrl");
    public static final QName WF_PROP_REJECT_URL = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "rejectUrl");
    public static final QName WF_PROP_INVITE_TICKET = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteTicket");
    public static final QName WF_PROP_INVITER_USER_NAME = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviterUserName");
    public static final QName WF_PROP_INVITEE_USER_NAME = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteeUserName");
    public static final QName WF_PROP_INVITEE_FIRSTNAME = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteeFirstName");
    public static final QName WF_PROP_INVITEE_LASTNAME = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteeLastName");
    public static final QName WF_PROP_SITE_SHORT_NAME = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "siteShortName");
    public static final QName WF_PROP_INVITEE_SITE_ROLE = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteeSiteRole");
    public static final QName WF_PROP_SENT_INVITE_DATE = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "sentInviteDate");
    public static final QName WF_PROP_INVITEE_GEN_PASSWORD = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteeGenPassword");
    
}
