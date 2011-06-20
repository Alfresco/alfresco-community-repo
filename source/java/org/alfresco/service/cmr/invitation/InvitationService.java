/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;

/**
 * The invitation service provides the ability to invite
 * people to resources.    For example adding a user to a shared web site.
 * 
 * It manages the relationship between person, resource and requestType 
 * and may also pass along information such as who is to approve or the expected 
 * role of the user.
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
	 * Start the invitation process for a NominatedInvitation for a user who does not yet have an Alfresco User Name
	 * 
	 * @param inviteeUserName Alfresco user name of who should be invited
	 * @param ResourceType resourceType
	 * @param resourceName
	 * @param inviteeRole
	 * @param serverPath
	 * @param acceptUrl
	 * @param rejectUrl
	 * 
	 * @return the nominated invitation which will contain the invitationId and
	 *         ticket which will uniqely identify this invitation for the rest
	 *         of the workflow.
	 * 
	 * @throws InvitationException
	 * @throws InvitationExceptionUserError
	 * @throws InvitationExceptionForbidden
	 */
    @Auditable(parameters = {"inviteeUserName", 
			"resourceType",
			"resourceName", 
			"inviteeRole", 
			"serverPath",
			"acceptUrl", 
			"rejectUrl"})
			
	public NominatedInvitation inviteNominated(
			String inviteeUserName, 
			Invitation.ResourceType resourceType,
			String resourceName, 
			String inviteeRole, 
			String serverPath,
			String acceptUrl, 
			String rejectUrl) ;

	/**
	 * Start the invitation process for a NominatedInvitation for a user who does not yet have an 
	 * Alfresco User NameA new user name will be generated as part of the invitation process.
	 * 
	 * @param inviteeFirstName
	 * @param inviteeLastName
     * @param inviteeEmail
     * @param Invitation.ResourceType resourceType
     * @param resourceName
     * @param inviteeRole 
     * @param serverPath 
     * @param acceptUrl 
     * @param rejectUrl
	 * 
	 * @return the nominated invitation which will contain the invitationId and ticket which 
	 * will uniquely identify this invitation.
	 * 
	 * @throws InvitationException
	 * @throws InvitationExceptionUserError
	 * @throws InvitationExceptionForbidden
	 */
    @Auditable(parameters = {
    		"inviteeFirstName", 
			"inviteeLastName",
            "inviteeEmail", 
            "resourceType",
            "resourceName", 
            "inviteeRole", 
            "serverPath", 
            "acceptUrl", 
            "rejectUrl"})
	public NominatedInvitation inviteNominated(
			String inviteeFirstName, 
			String inviteeLastName,
            String inviteeEmail, 
            Invitation.ResourceType resourceType,
            String resourceName, 
            String inviteeRole, 
            String serverPath, 
            String acceptUrl, 
            String rejectUrl);
	
	/**
	 * Start the invitation process for a ModeratedInvitation
     * @param inviteeUserName who is to be invited
     * @param Invitation.ResourceType resourceType  what resource type ?
     * @param resourceName which resource
     * @param inviteeRole which role ?
	 */
    @Auditable(parameters = {
    		"inviteeComments",
			"inviteeUserName",
			"resourceType",
			"resourceName", 
			"inviteeRole"})
	public ModeratedInvitation inviteModerated(
			String inviteeComments,
			String inviteeUserName,
			Invitation.ResourceType resourceType,
			String resourceName, 
			String inviteeRole);
	
	/**
	 * For a Nominated Invitation invitee accepts this invitation
	 * 
	 * @param request
	 * @param ticket
	 * @return the invitation
	 */
    @Auditable(parameters = {"invitationId", "reason"})    
	public Invitation accept(String invitationId, String ticket);
	

	/**
	 * Moderator approves this invitation
	 * 
	 * @param invitationId the request to approve
	 * @param reason - comments about the acceptance
	 */
    @Auditable(parameters = {"invitationId", "reason"})
	public Invitation approve(String invitationId, String reason);

	/**
	 * User or moderator rejects this request
	 * @param invitationId
	 * @param reason
	 */        
   @Auditable(parameters = {"invitationId", "reason"})
	public Invitation reject(String invitationId, String reason);

	
	/**
	 * cancel this request
	 */
   @Auditable(parameters = {"invitationId"})
	public Invitation cancel (String invitationId);
	
	/**
	 * get an invitation from its invitation id 
	 * 
	 * @param invitationId;
	 */
   @NotAuditable
	public Invitation getInvitation(String invitationId) ;
	
	/**
	 * list Invitations for a specific person
	 */
   @NotAuditable
	public List<Invitation> listPendingInvitationsForInvitee(String invitee);
	
	/**
	 * list Invitations for a specific resource
	 * @param resourceType
	 * @param resourceName
	 */
   @NotAuditable
	public List<Invitation> listPendingInvitationsForResource(Invitation.ResourceType resourceType, String resourceName);
		
    /**
     * search invitation
     * 
     * @param criteria
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
