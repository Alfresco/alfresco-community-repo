
package org.alfresco.repo.invitation.script;

import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;

/**
 * Java script invitation for the Java Script API
 * 
 * @author mrogers
 */
public abstract class ScriptInvitation<T extends Invitation>
{
    private T invitation;
    private InvitationService invitationService;

    public ScriptInvitation(T invitation, InvitationService invitationService)
    {
        this.invitation = invitation;
        this.invitationService = invitationService;
    }

    public void reject(String reason)
    {
        invitationService.reject(invitation.getInviteId(), reason);
    }
    
    public void cancel()
    {
        invitationService.cancel(invitation.getInviteId());
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
    
    protected T getInvitation()
    {
        return invitation;
    }
    
    protected InvitationService getInvitationService()
    {
        return invitationService;
    }

    /**
     * Which role to be added with
     * @return the roleName
     */
    public String getRoleName()
    {
        return getInvitation().getRoleName();
    }

    /**
     * The inviteeUserName
     * @return the invitee user name
     */
    public String getInviteeUserName()
    {
        return getInvitation().getInviteeUserName();
    }
    
    public abstract String getInviteeEmail();
    public abstract String getInviteeFirstName();
    public abstract String getInviteeLastName();
}
