/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.InvitationType;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;

/* package scope */ abstract class InvitationImpl 
{
    /**
     * Unique reference for this invitation
     */
    private String inviteId;
     
    /**
     * Which resource is this invitation for ?
     */
    private String resourceName;
     
    /**
     * What sort of invitation is this invitation for e.g. WEB_SITE or WEB_PROJECT
     */
    private Invitation.ResourceType resourceType;

    /**
     * What role is the invitation for.
     */
    private String roleName;

    /**
     * Who is this invitation for
     */
    private String inviteeUserName;
     
    /**
     * What sort of resource is it
     * @return the resource type
     */
    public ResourceType getResourceType()
    {
        return resourceType;
    }
        
    public void setResourceType(ResourceType resourceType)
    {
        this.resourceType = resourceType;
    }

    public void setInviteId(String inviteId)
    {
        this.inviteId = inviteId;
    }

    public String getInviteId()
    {
        return inviteId;
    }

    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }

    public String getResourceName()
    {
        return resourceName;
    }

    public String getRoleName()
    {
        return roleName;
    }
    
    /**
     * @param roleName the roleName to set
     */
    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    public abstract InvitationType getInvitationType();

    public void setInviteeUserName(String inviteeUserName)
    {
        this.inviteeUserName = inviteeUserName;
    }

    public String getInviteeUserName()
    {
        return inviteeUserName;
    }
}
