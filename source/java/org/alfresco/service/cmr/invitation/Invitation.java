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
package org.alfresco.service.cmr.invitation;

import java.util.Date;


/**
 * The invitation request is a command object for who, needs to be added or removed 
 * from which resource with which attributes.
 * 
 * Invitations are processed by the InvitationService
 * 
 * @see org.alfresco.service.cmr.invitation.InvitationService
 *
 * @author mrogers
 */
public interface Invitation 
{
	/**
	 * What sort of Resource   Web Project, Web Site, Node 
	 * (Just Web site for now) 
	 */
	enum ResourceType 
	{
		WEB_SITE
	}
	
	/**
	 * What type of invitation are we? 
	 * (Just Web site for now) 
	 */
	enum InvitationType 
	{
		NOMINATED,
		MODERATED
	}
	
	/**
	 * What sort of resource is it, for example a WEB_SITE?
	 * @return the resource type
	 */
	public ResourceType getResourceType();
	
	/**
	 * What is the resource name ?
	 * @return the name of the resource
	 */
	public String getResourceName();
	
	/**
	 * What is the unique reference for this invitation ?
	 * @return the unique reference for this invitation
	 */
	public String getInviteId();
	
	/**
	 * What sort of invitation is this ?
	 */
	public InvitationType getInvitationType();
	
    /**
     * Who wants to be added 
     * @return inviteeUserName
     */
    public String getInviteeUserName();

    /**
     * Which role to be added with
     * @return the roleName
     */
    public String getRoleName();
    
    Date getCreatedAt();
    
    Date getModifiedAt();
}
