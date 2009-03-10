/*
 * Copyright (C) 2009 Alfresco Software Limited.
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
