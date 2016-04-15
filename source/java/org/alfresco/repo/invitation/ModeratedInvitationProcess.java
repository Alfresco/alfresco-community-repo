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
     * @param request Invitation
     * @param reason String
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
