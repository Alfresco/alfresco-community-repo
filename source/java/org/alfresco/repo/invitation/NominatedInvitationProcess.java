package org.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.Invitation;

/**
 * The Invitation process is the interface provided by the invitation service to be 
 * implemented by each resource handler
 *
 * This invitation process is where someone nominates an invitee who then needs to accept or 
 * reject the nomination. 
 */

public interface NominatedInvitationProcess  extends InvitationProcess
{
    /*
     * inviter starts the invitation process
     */
    public Invitation invite(Invitation request, String comment);

    /**
     * invitee accepts this request
     * @param request Invitation
     */
    public void accept(Invitation request);

    /**
     * invitee rejects this request
     * @param request Invitation
     */
    public void reject(Invitation request);

    /**
     * cancel this request
     */
    public void cancel (Invitation request);
}
