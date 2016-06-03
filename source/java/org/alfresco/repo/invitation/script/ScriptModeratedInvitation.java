
package org.alfresco.repo.invitation.script;

import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;

/**
 * Java script moderated invitation for the Java Script API
 * 
 * @author mrogers
 */
public class ScriptModeratedInvitation extends ScriptInvitation<ModeratedInvitation> implements java.io.Serializable
{
    private static final long serialVersionUID = 4285823431857215500L;

    private final String inviteeEmail;
    private final String inviteeFirstName;
    private final String inviteeLastName;
    
    public ScriptModeratedInvitation(ModeratedInvitation invitation,
                InvitationService invitationService,
                String inviteeEmail,
                String inviteeFirstName,
                String inviteeLastName)
    {
        super(invitation, invitationService);
        this.inviteeEmail = inviteeEmail;
        this.inviteeFirstName = inviteeFirstName;
        this.inviteeLastName = inviteeLastName;
    }

    public void approve(String reason)
    {
        getInvitationService().approve(getInviteId(), reason);
    }

    /**
     * The invitee comments - why does the invitee want access ?
     * @return invitee comments
     */
    public String getInviteeComments()
    {
        return getInvitation().getInviteeComments();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.invitation.script.ScriptInvitation#getInviteeEmail()
     */
    @Override
    public String getInviteeEmail()
    {
        return inviteeEmail;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.invitation.script.ScriptInvitation#getInviteeFirstName()
     */
    @Override
    public String getInviteeFirstName()
    {
        return inviteeFirstName;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.invitation.script.ScriptInvitation#getInviteeLastName()
     */
    @Override
    public String getInviteeLastName()
    {
        return inviteeLastName;
    }
}
