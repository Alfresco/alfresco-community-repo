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

/**
 * Java script invitation for the Java Script API
 * 
 * @author mrogers
 */
public class ScriptInvitation 
{
	private Invitation invitation;
	private InvitationService invitationService;
					
	public ScriptInvitation(Invitation invitation, InvitationService invitationService)
	{
		this.invitation = invitation;
		this.invitationService = invitationService;				
	}
	
	public String getInviteId() 
	{
		return invitation.getInviteId();
	}

	public String getInvitationType() 
	{
		return invitation.getInvitationType().toString();
	}

	public String getResourceName() 
	{
		return invitation.getResourceName();
	}

	public String getResourceType() 
	{
		return invitation.getResourceType().toString();
	}
	
	protected Invitation getInvitation()
	{
		return invitation;
	}
	
	protected InvitationService getInvitationService()
	{
		return invitationService;
	}
}
