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
 * Represents a network quota.
 * 
 * @author steveglover
 *
 */
public class Quota
{
	private String name;
	private Long limit;
	private Long usage;
	
	public Quota()
	{
	}

	public Quota(String name, Long limit, Long usage)
	{
		this.name = name;
		this.limit = limit;
		this.usage = usage;
	}

	@UniqueId
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public Long getLimit()
	{
		return limit;
	}

	public void setLimit(Long limit)
	{
		this.limit = limit;
	}

	public Long getUsage()
	{
		return usage;
	}

	public void setUsage(Long usage)
	{
		this.usage = usage;
	}

	@Override
	public String toString()
	{
		return "Quota [name=" + name + ", limit=" + limit + ", usage=" + usage
				+ "]";
	}

}
