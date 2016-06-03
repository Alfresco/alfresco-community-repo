package org.alfresco.repo.invitation.site;

import org.alfresco.repo.invitation.ModeratedInvitationProcess;
import org.alfresco.service.cmr.invitation.Invitation;

/**
 * The Site Private Invitation Process implements the PrivateInvitatonProcess for 
 * Web Sites. 
 */
public class SiteModeratedInvitationProcess implements ModeratedInvitationProcess
{
    public void approve(Invitation request, String reason) 
    {
        // TODO Auto-generated method stub
    }
    
    public void cancel(Invitation request, String reason) 
    {
        // TODO Auto-generated method stub
    }
    
    public void reject(Invitation request, String reason) 
    {
        // TODO Auto-generated method stub
    }
    
    public Invitation invite(Invitation request, String reason) 
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void cancel(Invitation request) 
    {
        // TODO Auto-generated method stub
    }
}
