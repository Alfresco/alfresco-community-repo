package org.alfresco.service.cmr.invitation;

import java.util.Date;


/**
 * The nominated invitation is a model object for who, needs to be added or removed 
 * from which resource with which attributes.
 * 
 * Invitations are processed by the InvitationService
 * 
 * @see org.alfresco.service.cmr.invitation.InvitationService
 *
 * @author mrogers
 */
public interface NominatedInvitation extends Invitation
{
	public String getInviterUserName();

	public String getInviteeFirstName();

	public String getInviteeLastName();

	public String getInviteeEmail();

	public String getResourceName();

    public String getResourceTitle();

    public String getResourceDescription();

	public String getServerPath();

	public String getAcceptUrl();

	public String getRejectUrl();

	public Date getSentInviteDate();
	
	public String getTicket();
}
