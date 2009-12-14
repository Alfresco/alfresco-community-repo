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
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import java.util.Date;

/**
 * Java script moderated invitation for the Java Script API
 * 
 * @author mrogers
 */
public class ScriptNominatedInvitation  extends ScriptInvitation implements java.io.Serializable
{			
	/**
	 * 
	 */
	private static final long serialVersionUID = 6079656007339750930L;

	/**
	 * 
	 */

	public ScriptNominatedInvitation(Invitation invitation, InvitationService invitationService)
	{
		super(invitation, invitationService);
	}

	
	public void accept(String reason)
	{
		getInvitationService().accept(getInviteId(), reason);
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
		return ((NominatedInvitation)getInvitation()).getRoleName();
	}
	
	public String getInviteeUserName() 
	{
		return ((NominatedInvitation)getInvitation()).getInviteeUserName();
	}
	
	/**
	 * Which role to be added with
	 * @return the roleName
	 */
	public Date getSentInviteDate()
	{
		return ((NominatedInvitation)getInvitation()).getSentInviteDate();
	}
	
	public String getSentInviteDateAsISO8601()
	{
		return ISO8601DateFormat.format(getSentInviteDate());
	}

}