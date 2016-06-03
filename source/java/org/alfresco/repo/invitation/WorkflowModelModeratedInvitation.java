package org.alfresco.repo.invitation;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Workflow Model for a Moderated Invitation
 */
public interface WorkflowModelModeratedInvitation
{
    // namespace
    public static final String NAMESPACE_URI = "http://www.alfresco.org/model/workflow/invite/moderated/1.0";
    
    // process name
    public static final QName WF_PROCESS_INVITATION_MODERATED = QName.createQName(NAMESPACE_URI, "invitation-moderated");

    // workflow definition name
    public static final String WORKFLOW_DEFINITION_NAME = "jbpm$imwf:invitation-moderated";
    public static final String WORKFLOW_DEFINITION_NAME_ACTIVITI = "activiti$activitiInvitationModerated";
    
    // tasks
    public static final QName WF_START_TASK = QName.createQName(NAMESPACE_URI, "moderatedInvitationSubmitTask");
    public static final QName WF_REVIEW_TASK =  QName.createQName(NAMESPACE_URI,"moderatedInvitationReviewTask");
    public static final QName WF_ACTIVITI_REVIEW_TASK =  QName.createQName(NAMESPACE_URI,"activitiModeratedInvitationReviewTask");
    
    // associations
    static final QName ASSOC_GROUP_ASSIGNEE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "groupAssignee");
    
    // transition names
    public static final String WF_TRANSITION_REVIEW = "review";
    public static final String WF_TRANSITION_APPROVE = "approve";
    public static final String WF_TRANSITION_REJECT = "reject";
    public static final String WF_TRANSITION_CANCEL = "cancel";
    public static final String WF_TRANSITION_END = "end";
    
    // workflow properties
    public static final QName WF_PROP_INVITEE_USER_NAME = QName.createQName(NAMESPACE_URI, "inviteeUserName");
    public static final QName WF_PROP_INVITEE_ROLE = QName.createQName(NAMESPACE_URI, "inviteeRole");
    public static final QName WF_PROP_INVITEE_COMMENTS = QName.createQName(NAMESPACE_URI, "inviteeComments");
    public static final QName WF_PROP_RESOURCE_NAME = QName.createQName(NAMESPACE_URI, "resourceName");
    public static final QName WF_PROP_RESOURCE_TYPE= QName.createQName(NAMESPACE_URI, "resourceType");
    public static final QName WF_PROP_REVIEW_COMMENTS= QName.createQName(NAMESPACE_URI, "reviewComments");
    public static final QName WF_PROP_REVIEWER= QName.createQName(NAMESPACE_URI, "reviewer");
    public static final QName WF_PROP_MODIFIED_AT= QName.createQName(NAMESPACE_URI, "modifiedAt");

    // workflow execution context variable names
    public static final String wfVarInviteeUserName = "imwf_inviteeUserName";
    public static final String wfVarInviteeRole = "imwf_inviteeRole";
    public static final String wfVarWorkflowInstanceId = "workflowinstanceid";
    public static final String wfVarResourceName = "imwf_resourceName";
    public static final String wfVarResourceType = "imwf_resourceType";
    public static final String wfVarReviewer = "imwf_reviewer";
    public static final String wfVarReviewComments = "imwf_reviewComments";
}
