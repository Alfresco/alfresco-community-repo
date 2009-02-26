package org.alfresco.repo.invitation;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

/**
 * Workflow Model for a Moderated Invitation
 */
public interface WorkflowModelModeratedInvitation {

    // process name
    public static final QName WF_PROCESS_INVITATION_MODERATED = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "invitation-moderated");

    // workflow definition name
    public static final String WORKFLOW_DEFINITION_NAME = "jbpm$wf:invitation-moderated";
    
    // tasks
    public static final QName WF_START_TASK = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "moderatedInvitationSubmitTask");
    public static final QName WF_REVIEW_TASK =  QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI,"moderatedInvitationReviewTask");
    
    // associations
    static final QName ASSOC_GROUP_ASSIGNEE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "groupAssignee");
    
    // transition names
    public static final String WF_TRANSITION_REVIEW = "review";
    public static final String WF_TRANSITION_APPROVE = "approve";
    public static final String WF_TRANSITION_REJECT = "reject";
    public static final String WF_TRANSITION_CANCEL = "cancel";
    public static final String WF_TRANSITION_END = "end";
    
    // workflow properties
    public static final QName WF_PROP_INVITEE_USER_NAME = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteeUserName");
    public static final QName WF_PROP_INVITEE_ROLE = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteeRole");
    public static final QName WF_PROP_INVITEE_COMMENTS = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "inviteeComments");
    public static final QName WF_PROP_RESOURCE_NAME = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "resourceName");
    public static final QName WF_PROP_RESOURCE_TYPE= QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "resourceType");
    public static final QName WF_PROP_REVIEW_COMMENTS= QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewComments");
    public static final QName WF_PROP_REVIEWER= QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewer");
    
    // workflow execution context variable names
    public static final String wfVarInviteeUserName = "wf_inviteeUserName";
    public static final String wfVarInviteeRole = "wf_inviteeRole";
    public static final String wfVarWorkflowInstanceId = "workflowinstanceid";
    public static final String wfVarResourceName = "wf_resourceName";
    public static final String wfVarResourceType = "wf_resourceType";
    public static final String wfVarReviewer = "wf_reviewer";
    public static final String wfVarReviewComments = "wf_reviewComments";
    }
