package org.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.Invitation;

/**
 * The Invitation process is the interface provided by the invitation service to be 
 * implemented by each resource's invitation handler
 *
 * This invitation process is the unmoderated invite someone else.
 */
public interface InvitationProcess 
{
    /*
     * someone starts the invitation process
     */
    public Invitation invite(Invitation request, String comment);

    /*
     * cancel this request
     */
    public void cancel (Invitation request);
}
