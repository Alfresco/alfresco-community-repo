/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
