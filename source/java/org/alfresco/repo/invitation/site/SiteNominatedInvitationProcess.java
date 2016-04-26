package org.alfresco.repo.invitation.site;

import org.alfresco.repo.invitation.NominatedInvitationProcess;
import org.alfresco.service.cmr.invitation.Invitation;

/**
 * The Site Private Invitation Process implements the PrivateInvitatonProcess for 
 * Web Sites. 
 */
public class SiteNominatedInvitationProcess implements NominatedInvitationProcess
{
    /**
     * inviter starts the invitation process
     */
    public Invitation invite(Invitation request, String reason)
    {
        return null;
    }
    
    /**
     * invitee accepts this request
     * @param request Invitation
     */
    public void accept(Invitation request)
    {
    }
    
    /**
     * invitee rejects this request
     * @param request Invitation
     */
    public void reject(Invitation request)
    {
    }
    
    /**
     * cancel this request
     */
    public void cancel (Invitation request)
    {
    }
}
