
package org.alfresco.repo.invitation.script;

import java.util.Date;

import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Java script moderated invitation for the Java Script API
 * 
 * @author mrogers
 */
public class ScriptNominatedInvitation  extends ScriptInvitation<NominatedInvitation> implements java.io.Serializable
{
    private static final long serialVersionUID = 6079656007339750930L;

    public ScriptNominatedInvitation(NominatedInvitation invitation, InvitationService invitationService)
    {
        super(invitation, invitationService);
    }

    /**
     * @see org.alfresco.service.cmr.invitation.NominatedInvitation#getInviteeEmail()
     */
    @Override
    public String getInviteeEmail()
    {
        return getInvitation().getInviteeEmail();
    }

    /**
     * @see org.alfresco.service.cmr.invitation.NominatedInvitation#getInviteeFirstName()
     */
    @Override
    public String getInviteeFirstName()
    {
        return getInvitation().getInviteeFirstName();
    }

    /**
     * @see org.alfresco.service.cmr.invitation.NominatedInvitation#getInviteeLastName()
     */
    @Override
    public String getInviteeLastName()
    {
        return getInvitation().getInviteeLastName();
    }

    public void accept(String reason)
    {
        getInvitationService().accept(getInviteId(), reason);
    }
    
    /**
     * Which role to be added with
     * @return the roleName
     */
    public Date getSentInviteDate()
    {
        return getInvitation().getSentInviteDate();
    }
    
    public String getSentInviteDateAsISO8601()
    {
        return ISO8601DateFormat.format(getSentInviteDate());
    }

}