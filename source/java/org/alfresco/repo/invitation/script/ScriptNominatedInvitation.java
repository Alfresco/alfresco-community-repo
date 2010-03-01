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