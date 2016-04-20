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
