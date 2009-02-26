/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.Invitation;

/**
 * The Moderated Invitation Process has a moderator who approves or rejects 
 * invitations raised by the invitee themselves.
 *
 * Upon approval the invitee will be given the requested role for the 
 * requested resource.
 */

public interface ModeratedInvitationProcess extends InvitationProcess
{
	/**
	 * Invitee kicks off process
	 * @param request
	 * @param reason
	 */
	public Invitation invite(Invitation request, String reason);
	
	/**
	 * Moderator approves this request
	 * @param request the request to approve.
	 */
	public void approve(Invitation request, String reason);
	
	/**
	 * Moderator rejects this request
	 * @param request the request to reject
	 */
	public void reject(Invitation request, String reason);
	
	/**
	 * Invitee cancels this request
	 */
	public void cancel (Invitation request, String reason);
	
}
