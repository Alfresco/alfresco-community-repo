package org.alfresco.service.cmr.invitation;

import java.util.Date;


/**
 * The invitation request is a command object for who, needs to be added or removed 
 * from which resource with which attributes.
 * 
 * Invitations are processed by the InvitationService
 * 
 * @see org.alfresco.service.cmr.invitation.InvitationService
 *
 * @author mrogers
 */
public interface Invitation 
{
	/**
	 * What sort of Resource   Web Project, Web Site, Node 
	 * (Just Web site for now) 
	 */
	enum ResourceType 
	{
		WEB_SITE
	}
	
	/**
	 * What type of invitation are we? 
	 * (Just Web site for now) 
	 */
	enum InvitationType 
	{
		NOMINATED,
		MODERATED
	}
	
	/**
	 * What sort of resource is it, for example a WEB_SITE?
	 * @return the resource type
	 */
	public ResourceType getResourceType();
	
	/**
	 * What is the resource name ?
	 * @return the name of the resource
	 */
	public String getResourceName();
	
	/**
	 * What is the unique reference for this invitation ?
	 * @return the unique reference for this invitation
	 */
	public String getInviteId();
	
	/**
	 * What sort of invitation is this ?
	 */
	public InvitationType getInvitationType();
	
    /**
     * Who wants to be added 
     * @return inviteeUserName
     */
    public String getInviteeUserName();

    /**
     * Which role to be added with
     * @return the roleName
     */
    public String getRoleName();
    
    Date getCreatedAt();
    
    Date getModifiedAt();
}
