/*
 * Copyright (C) 2009-2011 Alfresco Software Limited.
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
