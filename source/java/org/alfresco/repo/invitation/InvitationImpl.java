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
package org.alfresco.repo.invitation;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.InvitationType;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;

/* package scope */ abstract class InvitationImpl 
{
    public static final String ID_KEY = "id";
    public static final String INVITEE_KEY = "invitee";
    public static final String RESOURCE_NAME_KEY = "resourceName";
    public static final String RESOURCE_TYPE_KEY = "resourceType";
    public static final String ROLE_KEY = "role";
    public static final String CREATED_AT = "createdAt";
    public static final String MODIFIED_AT = "modifiedAt";
    
    /**
     * Unique reference for this invitation
     */
    private final String inviteId;
     
    /**
     * Which resource is this invitation for ?
     */
    private final String resourceName;
     
    /**
     * What sort of invitation is this invitation for e.g. WEB_SITE or WEB_PROJECT
     */
    private final Invitation.ResourceType resourceType;

    /**
     * What role is the invitation for.
     */
    private final String roleName;

    /**
     * Who is this invitation for
     */
    private final String inviteeUserName;
    
    private final Date createdAt;
    
    private final Date modifiedAt;
     
    public InvitationImpl(Map<String, Serializable> props)
    {
        this.inviteId = (String)props.get(ID_KEY);
        this.inviteeUserName = (String)props.get(INVITEE_KEY);
        this.resourceName = (String)props.get(RESOURCE_NAME_KEY);
        this.roleName = (String)props.get(ROLE_KEY);
        String type = (String)props.get(RESOURCE_TYPE_KEY);
        this.resourceType = type==null ? ResourceType.WEB_SITE : ResourceType.valueOf(type);
        this.createdAt = (Date)props.get(CREATED_AT);
        this.modifiedAt = (Date)props.get(MODIFIED_AT);
    }

    /**
     * What sort of resource is it
     * @return the resource type
     */
    public ResourceType getResourceType()
    {
        return resourceType;
    }

    public Date getCreatedAt()
    {
		return createdAt;
	}

	public Date getModifiedAt()
	{
		return modifiedAt;
	}

	public String getInviteId()
    {
        return inviteId;
    }

    public String getResourceName()
    {
        return resourceName;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public String getInviteeUserName()
    {
        return inviteeUserName;
    }
    
    public abstract InvitationType getInvitationType();
}
