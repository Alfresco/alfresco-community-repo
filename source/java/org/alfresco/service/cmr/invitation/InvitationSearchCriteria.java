package org.alfresco.service.cmr.invitation;

/**
 * Search criteria for invitation service
 *
 */
public interface InvitationSearchCriteria 
{
	/**
	 * What type of invitations to search for ?
	 *
	 */
	public enum InvitationType
	{
		ALL,
		MODERATED,
		NOMINATED
	}

	
	/**
	 * Search by inviter (who started the invitation)
	 * @return String
	 */
	String getInviter();
	
	/**
	 * Search by invitee  (who is being invited, alfresco userid)
	 * @return String
	 */
	String getInvitee();
	
	/** 
	 * Search by resource name
	 * @return the resource name
	 */
	String getResourceName();
	
	/**
	 * Search by resource type
	 * @return the resource type
	 */
	Invitation.ResourceType getResourceType();
	
	/**
	 * Do you want to search for moderated, nominated or all invitations ?
	 * @return the type to search for.
	 */
	InvitationType getInvitationType();
}
