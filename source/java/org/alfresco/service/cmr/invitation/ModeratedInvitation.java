package org.alfresco.service.cmr.invitation;


/**
 * The moderated invitation request is a model object for who, needs to be added or removed 
 * from which resource with which attributes.
 * 
 * Invitations are processed by the InvitationService
 * 
 * @see org.alfresco.service.cmr.invitation.InvitationService
 *
 * @author mrogers
 */
public interface ModeratedInvitation extends Invitation
{	
	/**
	 * The invitee comments - why does the invitee want access ?
	 * @return invitee comments
	 */
	public String getInviteeComments();

}
