/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;

public class InvitationSearchCriteriaImpl implements InvitationSearchCriteria
{
    private String invitee;
    private String inviter;
    private String resourceName;
    private ResourceType resourceType;
    private InvitationSearchCriteria.InvitationType invitationType = InvitationSearchCriteria.InvitationType.ALL;

    public void setInvitee(String invitee) 
    {
        this.invitee = invitee;
    }
    
    public String getInvitee() 
    {
        return invitee;
    }
    
    public void setInviter(String inviter) 
    {
        this.inviter = inviter;
    }
    
    public String getInviter() 
    {
        return inviter;
    }
    
    public void setResourceName(String resourceName) 
    {
        this.resourceName = resourceName;
    }
    
    public String getResourceName() 
    {
        return resourceName;
    }
    
    public void setResourceType(ResourceType resourceType) 
    {
        this.resourceType = resourceType;
    }
    
    public ResourceType getResourceType() 
    {
        return resourceType;
    }
    
    public InvitationType getInvitationType() 
    {
        return invitationType;
    }
    
    public void setInvitationType(InvitationType invitationType)
    {
        this.invitationType = invitationType;
    }
}
