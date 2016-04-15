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
package org.alfresco.repo.favourites;

import java.util.Date;

import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Representation of a user's favourite site, document, folder.
 * 
 * @author steveglover
 *
 */
public class PersonFavourite
{
	private String userName;
	private String title;
	private Type type; // using a type rather then subclassing to make sorting of PersonFavourites easier TODO
	private Date createdAt;
	private NodeRef nodeRef;

    public static class PersonFavouriteKey
    {
    	private String userName;
    	private Type type;
    	private String title;
    	private NodeRef nodeRef;
    	private Date createdAt;
    	
		public PersonFavouriteKey(String userName, String title, Type type, NodeRef nodeRef)
		{
			super();
			this.userName = userName;
			this.type = type;
			this.nodeRef = nodeRef;
		}

		public PersonFavouriteKey(String userName, String title, Type type, NodeRef nodeRef, Date createdAt)
		{
			super();
			this.userName = userName;
			this.type = type;
			this.nodeRef = nodeRef;
			this.createdAt = createdAt;
		}
		
		public String getTitle()
		{
			return title;
		}

		public String getUserName()
		{
			return userName;
		}

		public Type getType()
		{
			return type;
		}

		public Date getCreatedAt()
		{
			return createdAt;
		}

		public NodeRef getNodeRef()
		{
			return nodeRef;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((nodeRef == null) ? 0 : nodeRef.hashCode());
			result = prime * result
					+ ((userName == null) ? 0 : userName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PersonFavouriteKey other = (PersonFavouriteKey) obj;
			if (nodeRef == null)
			{
				if (other.nodeRef != null)
					return false;
			} else if (!nodeRef.equals(other.nodeRef))
				return false;
			if (userName == null)
			{
				if (other.userName != null)
					return false;
			} else if (!userName.equals(other.userName))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "PersonFavouriteKey [userName=" + userName + ", nodeRef="
					+ nodeRef + "]";
		}
    }

	/*
	 * Used for comparisons
	 */
	PersonFavourite(String userName, NodeRef nodeRef, Type type)
	{
		super();
		if(userName == null)
		{
			throw new IllegalArgumentException("Must provide a userName");
		}
		if(nodeRef == null)
		{
			throw new IllegalArgumentException("Must provide a nodeRef");
		}
		if(type == null)
		{
			throw new IllegalArgumentException("Must provide a type");
		}
		this.userName = userName;
		this.nodeRef = nodeRef;
		this.type = type;
	}

	public PersonFavourite(String userName, NodeRef nodeRef, Type type, String title, Date createdAt)
	{
		super();
		if(userName == null)
		{
			throw new IllegalArgumentException("Must provide a userName");
		}
		if(nodeRef == null)
		{
			throw new IllegalArgumentException("Must provide a nodeRef");
		}
		if(type == null)
		{
			throw new IllegalArgumentException("Must provide a type");
		}
		if(title == null)
		{
			throw new IllegalArgumentException("Must provide a title");
		}
		// re-instate if Share can persist createdAt for favourites
//		if(createdAt == null)
//		{
//			throw new IllegalArgumentException("Must provide a createdAt");
//		}
		this.userName = userName;
		this.nodeRef = nodeRef;
		this.type = type;
		this.title = title;
		this.createdAt = createdAt;
	}
	
//	PersonFavourite(String userName, NodeRef nodeRef, Type type, String title, Date createdAt, boolean exists)
//	{
//		this(userName, nodeRef, type, title, createdAt);
//		this.exists = exists;
//	}

	public PersonFavouriteKey getKey()
	{
		PersonFavouriteKey key = new PersonFavouriteKey(getUserName(), getTitle(), getType(), getNodeRef(), getCreatedAt());
		return key;
	}

	public String getTitle()
	{
		return title;
	}
	
	public String getUserName()
	{
		return userName;
	}

	public Type getType()
	{
		return type;
	}

	public NodeRef getNodeRef()
	{
		return nodeRef;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonFavourite other = (PersonFavourite) obj;
		if (nodeRef == null)
		{
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (userName == null)
		{
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return "PersonFavourite [userName=" + userName + ", name=" + title
				+ ", type=" + type + ", createdAt=" + createdAt + ", nodeRef="
				+ nodeRef + "]";
	}
}
