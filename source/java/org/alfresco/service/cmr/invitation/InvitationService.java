/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.service.cmr.invitation;

import java.util.List;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;

/**
 * The invitation service provides the ability to invite people to resources. For example adding a user to a shared web site. It manages the relationship between person, resource
 * and requestType and may also pass along information such as who is to approve or the expected role of the user.
 * 
 * @author mrogers
 */
public interface InvitationService
{
    /**
     * Get the names of the workflows managed by the invitation service.
     * 
     * @return the names of the workkflows managed by the invitation service.
     */
    @NotAuditable
    public List<String> getInvitationServiceWorkflowNames();

    /**
     * Start the invitation process for a NominatedInvitation for a user who does not yet have an Alfresco User Name The server path is calculated based on the sysAdmin parameters
     * 
     * @param inviteeUserName Alfresco user name of who should be invited
    * @param ResourceType resourceType
    * @param resourceName
    * @param inviteeRole
    * @param acceptUrl
    * @param rejectUrl
     * @return the nominated invitation which will contain the invitationId and ticket which will uniqely identify this invitation for the rest of the workflow.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    @Auditable(parameters = { "inviteeUserName", "resourceType", "resourceName", "inviteeRole", "acceptUrl", "rejectUrl" })
    public NominatedInvitation inviteNominated(String inviteeUserName, Invitation.ResourceType resourceType, String resourceName, String inviteeRole, String acceptUrl,
            String rejectUrl);

    /**
     * Start the invitation process for a NominatedInvitation for a user who does not yet have an Alfresco User NameA new user name will be generated as part of the invitation
     * process. The server path is calculated based on the sysAdmin parameters
     * 
    * @param inviteeFirstName
    * @param inviteeLastName
    * @param inviteeEmail
    * @param Invitation.ResourceType resourceType
    * @param resourceName
    * @param inviteeRole 
    * @param acceptUrl 
    * @param rejectUrl
     * @return the nominated invitation which will contain the invitationId and ticket which will uniquely identify this invitation.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    @Auditable(parameters = { "inviteeFirstName", "inviteeLastName", "inviteeEmail", "resourceType", "resourceName", "inviteeRole", "acceptUrl", "rejectUrl" })
    public NominatedInvitation inviteNominated(String inviteeFirstName, String inviteeLastName, String inviteeEmail, Invitation.ResourceType resourceType, String resourceName,
            String inviteeRole, String acceptUrl, String rejectUrl);

    /**
     * Start the invitation process for a NominatedInvitation for a user who does not yet have an Alfresco User Name
     * 
     * @param inviteeUserName Alfresco user name of who should be invited
	 * @param ResourceType resourceType
	 * @param resourceName
	 * @param inviteeRole
	 * @param serverPath
	 * @param acceptUrl
	 * @param rejectUrl
     * @return the nominated invitation which will contain the invitationId and ticket which will uniqely identify this invitation for the rest of the workflow.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    @Auditable(parameters = { "inviteeUserName", "resourceType", "resourceName", "inviteeRole", "serverPath", "acceptUrl", "rejectUrl" })
    public NominatedInvitation inviteNominated(String inviteeUserName, Invitation.ResourceType resourceType, String resourceName, String inviteeRole, String serverPath,
            String acceptUrl, String rejectUrl);

    /**
     * Start the invitation process for a NominatedInvitation for a user who does not yet have an Alfresco User NameA new user name will be generated as part of the invitation
     * process.
     * 
	 * @param inviteeFirstName
	 * @param inviteeLastName
     * @param inviteeEmail String
     * @param resourceType Invitation.ResourceType
     * @param resourceName String
     * @param inviteeRole 
     * @param serverPath 
     * @param acceptUrl 
     * @param rejectUrl String
     * @return the nominated invitation which will contain the invitationId and ticket which will uniquely identify this invitation.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    @Auditable(parameters = { "inviteeFirstName", "inviteeLastName", "inviteeEmail", "resourceType", "resourceName", "inviteeRole", "serverPath", "acceptUrl", "rejectUrl" })
    public NominatedInvitation inviteNominated(String inviteeFirstName, String inviteeLastName, String inviteeEmail, Invitation.ResourceType resourceType, String resourceName,
            String inviteeRole, String serverPath, String acceptUrl, String rejectUrl);

    /**
     * Start the invitation process for a ModeratedInvitation
     * 
     * @param inviteeUserName who is to be invited
     * @param Invitation.ResourceType resourceType  what resource type ?
     * @param resourceName which resource
     * @param inviteeRole which role ?
     */
    @Auditable(parameters = { "inviteeComments", "inviteeUserName", "resourceType", "resourceName", "inviteeRole" })
    public ModeratedInvitation inviteModerated(String inviteeComments, String inviteeUserName, Invitation.ResourceType resourceType, String resourceName, String inviteeRole);

    /**
     * Update the invitee comments for an existing moderated invitation
     * 
	 * @param inviteeId
	 * @param siteShortName
	 * @param inviteeComments
     * @return the invitation
     */
    @Auditable(parameters = { "inviteeId", "siteShortName", "inviteeComments" })
    ModeratedInvitation updateModeratedInvitation(String inviteeId, String siteShortName, String inviteeComments);

    /**
     * For a Nominated Invitation invitee accepts this invitation
     * 
	 * @param request
	 * @param ticket
     * @return the invitation
     */
    @Auditable(parameters = { "invitationId", "reason" })
    public Invitation accept(String invitationId, String ticket);

    /**
     * Moderator approves this invitation
     * 
     * @param invitationId the request to approve
     * @param reason - comments about the acceptance
     */
    @Auditable(parameters = { "invitationId", "reason" })
    public Invitation approve(String invitationId, String reason);

    /**
     * User or moderator rejects this request
     * 
	 * @param invitationId
	 * @param reason
     */
    @Auditable(parameters = { "invitationId", "reason" })
    public Invitation reject(String invitationId, String reason);

    /**
     * Moderator approves this invitation
     * 
     * @param siteName
     * @param invitee
     * @param role
     * @param inviter
     */
    @Auditable(parameters = { "siteName", "Invitee", "role", "inviter" })
    public void acceptNominatedInvitation(String siteName, String invitee, String role, String inviter);

    /**
     * Moderator approves this invitation
     * 
     * @param siteName
     * @param invitee
     * @param role
     * @param reviewer
     */
    @Auditable(parameters = { "siteName", "Invitee", "role", "reviewer" })
    public void approveModeratedInvitation(String siteName, String invitee, String role, String reviewer);

    /**
     * Moderator rejects this invitation
     * 
     * @param siteName
     * @param invitee
     * @param role
     * @param reviewer
     * @param resourceType
     * @param reviewComments
     */
    @Auditable(parameters = { "siteName", "Invitee", "role", "reviewer", "resourceType", "reviewComments" })
    public void rejectModeratedInvitation(String siteName, String invitee, String role, String reviewer, String resourceType, String reviewComments);

    /**
     * Inviter cancels this invitation
     * 
     * @param siteName
     * @param invitee
     * @param inviteId
     * @param currentInviteId
     */
    @Auditable(parameters = { "siteName", "Invitee", "inviteId", "currentInviteId" })
    public void cancelInvitation(String siteName, String invitee, String inviteId, String currentInviteId);

    /**
     * cancel this request
     */
    @Auditable(parameters = { "invitationId" })
    public Invitation cancel(String invitationId);

    /**
     * Clean up invitee user account and person node when no longer in use.
     * They are deemed to no longer be in use when the invitee user account
     * is still disabled and there are no outstanding pending invites for that invitee.
     * 
     * @param invitee
     * @param currentInviteId
     */
    @Auditable(parameters = { "Invitee", "currentInviteId" })
    public void deleteAuthenticationIfUnused(String invitee, String currentInviteId);
    
    /**
     * Implemented for backwards compatibility
     * 
     * @param inviteId
     * @param executionVariables
     * @deprecated
     * @see {@link #sendNominatedInvitation(String, String, String, Map)}
     */
    @Auditable(parameters = { "inviteId" })
    public void sendNominatedInvitation(String inviteId, Map<String, Object> executionVariables);
    
    /**
     * Sends the invite email using the given template, subject localization key, and variables.
     * 
     * @param inviteId
     * @param emailTemplateXpath the XPath to the email template in the repository
     * @param emailSubjectKey the subject of the email
     * @param executionVariables the variables used to populate the email
     */
    @Auditable(parameters = { "inviteId" })
    public void sendNominatedInvitation(String inviteId, String emailTemplateXpath, 
            String emailSubjectKey, Map<String, Object> executionVariables);

    /**
     * get an invitation from its invitation id
     * 
	 * @param invitationId;
     */
    @NotAuditable
    public Invitation getInvitation(String invitationId);

    /**
     * list Invitations for a specific person
     */
    @NotAuditable
    public List<Invitation> listPendingInvitationsForInvitee(String invitee);

    @NotAuditable
    public List<Invitation> listPendingInvitationsForInvitee(String invitee, Invitation.ResourceType resourceType);

    /**
     * list Invitations for a specific resource
     * 
	 * @param resourceType
	 * @param resourceName
     */
    @NotAuditable
    public List<Invitation> listPendingInvitationsForResource(Invitation.ResourceType resourceType, String resourceName);

    /**
     * search invitation
     * 
     * @param criteria InvitationSearchCriteria
     * @return the list of invitations
     */
    @NotAuditable
    public List<Invitation> searchInvitation(InvitationSearchCriteria criteria);

    /**
     * @return true if emails are sent on invite.
     */
    @NotAuditable
    boolean isSendEmails();

}
