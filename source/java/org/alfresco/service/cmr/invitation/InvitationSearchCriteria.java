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
package org.alfresco.service.cmr.invitation;

/**
 * Search criteria for invitation service
 *
 */
public interface InvitationSearchCriteria 
{
	/**
	 * What type of invitations to search for ?
	 *
	 */
	public enum InvitationType
	{
		ALL,
		MODERATED,
		NOMINATED
	}

	
	/**
	 * Search by inviter (who started the invitation)
	 * @return
	 */
	String getInviter();
	
	/**
	 * Search by invitee  (who is being invited, alfresco userid)
	 * @return
	 */
	String getInvitee();
	
	/** 
	 * Search by resource name
	 * @return the resource name
	 */
	String getResourceName();
	
	/**
	 * Search by resource type
	 * @return the resource type
	 */
	Invitation.ResourceType getResourceType();
	
	/**
	 * Do you want to search for moderated, nominated or all invitations ?
	 * @return the type to search for.
	 */
	InvitationType getInvitationType();
}
