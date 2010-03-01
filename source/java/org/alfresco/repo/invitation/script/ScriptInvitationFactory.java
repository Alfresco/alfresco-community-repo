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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;

public class ScriptInvitationFactory 
{
	public static ScriptInvitation toScriptInvitation(Invitation invitation, InvitationService invitationService)
	{
		if(invitation instanceof NominatedInvitation)
		{
			return new ScriptNominatedInvitation(invitation, invitationService);
		}
		
		if(invitation instanceof ModeratedInvitation)
		{
			return new ScriptModeratedInvitation(invitation, invitationService);
		}
		
		throw new AlfrescoRuntimeException("unknown invitation type");
	}
}
