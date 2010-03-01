/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.invitation.script;

import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;

/**
 * Java script moderated invitation for the Java Script API
 * 
 * @author mrogers
 */
public class ScriptModeratedInvitation extends ScriptInvitation implements java.io.Serializable
{
				
	/**
	 * 
	 */
	private static final long serialVersionUID = 4285823431857215500L;

	public ScriptModeratedInvitation(Invitation invitation, InvitationService invitationService)
	{
		super(invitation, invitationService);			
	}
	
	
	public void approve(String reason)
	{
		getInvitationService().approve(getInviteId(), reason);
	}
	
	public void reject(String reason)
	{
		getInvitationService().reject(getInviteId(), reason);
	}
	
	public void cancel()
	{
		getInvitationService().cancel(getInviteId());
	}
	
	/**
	 * Which role to be added with
	 * @return the roleName
	 */
	public String getRoleName()
	{
		return ((ModeratedInvitation)getInvitation()).getRoleName();
	}
	
	/**
	 * The invitee comments - why does the invitee want access ?
	 * @return invitee comments
	 */
	public String getInviteeComments()
	{
		return ((ModeratedInvitation)getInvitation()).getInviteeComments();
	}
    
	/**
	 * The inviteeUserName
	 * @return the invitee user name
	 */
	public String getInviteeUserName() 
	{
		return ((ModeratedInvitation)getInvitation()).getInviteeUserName();
	}
}
