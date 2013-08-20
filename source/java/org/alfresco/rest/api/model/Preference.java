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

import java.io.Serializable;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Represents a user preference.
 * 
 * @author steveglover
 *
 */
public class Preference implements Comparable<Preference>
{
	private String name;
	private Serializable value;
	
	public Preference()
	{
	}
	
	public Preference(String name, Serializable value)
	{
		if(name == null)
		{
			throw new IllegalArgumentException();
		}
		this.name = name;
		this.value = value;
	}

	@UniqueId
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		if(name == null)
		{
			throw new IllegalArgumentException();
		}
		this.name = name;
	}

	public Serializable getValue()
	{
		return value;
	}

	public void setValue(Serializable value)
	{
		this.value = value;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj == null)
		{
			return false;
		}
		
		if (getClass() != obj.getClass())
		{
			return false;
		}
		
		Preference other = (Preference) obj;
		return name.equals(other.name);
	}

	@Override
	public int compareTo(Preference preference)
	{
		return name.compareTo(preference.getName());
	}

	@Override
	public String toString()
	{
		return "Preference [name=" + name + ", value=" + value + "]";
	}
}
