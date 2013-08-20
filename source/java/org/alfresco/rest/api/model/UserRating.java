/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.model;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Represents a user rating for a node.
 * 
 * @author steveglover
 *
 */
public class UserRating
{
	private Float userRating;
	private String personId;

	public UserRating(String personId, Float userRating)
	{
		super();
		this.userRating = userRating;
		this.personId = personId;
	}

	public Float getUserRating()
	{
		return userRating;
	}

	@UniqueId
	public String getPersonId()
	{
		return personId;
	}

	@Override
	public String toString()
	{
		return "UserRating [userRating=" + userRating + ", personId="
				+ personId + "]";
	}
	
	
}
