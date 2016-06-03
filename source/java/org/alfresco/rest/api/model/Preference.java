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
