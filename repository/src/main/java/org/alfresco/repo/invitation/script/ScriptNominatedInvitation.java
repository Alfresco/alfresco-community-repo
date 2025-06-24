/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.invitation.script;

import java.util.Date;

import org.springframework.extensions.surf.util.ISO8601DateFormat;

import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;

/**
 * Java script moderated invitation for the Java Script API
 * 
 * @author mrogers
 */
public class ScriptNominatedInvitation extends ScriptInvitation<NominatedInvitation> implements java.io.Serializable
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
     * 
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

    public String getInviteTicket()
    {
        return getInvitation().getTicket();
    }
}
