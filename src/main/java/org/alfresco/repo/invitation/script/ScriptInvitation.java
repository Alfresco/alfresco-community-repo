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
