/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;

public class InvalidFavouriteTarget implements FavouritesTarget
{
	private String name;
	private Object entity;
	private String targetGuid;

	public static class GenericFavourite
	{
		private String guid;

		public String getGuid()
		{
			return guid;
		}

		public void setGuid(String guid)
		{
			this.guid = guid;
		}
		
		@SuppressWarnings("unchecked")
		public JSONObject toJSON()
		{
			JSONObject json = new JSONObject();
			json.put("guid", guid);
			return json;
		}

		@Override
		public String toString()
		{
			return "GenericFavourite [guid=" + guid + "]";
		}
	}

	public InvalidFavouriteTarget(String name, Object entity, String targetGuid)
	{
		super();
		this.name = name;
		this.entity = entity;
		this.targetGuid = targetGuid;
	}

	public Object getEntity()
	{
		return entity;
	}

	public void setEntity(Object entity)
	{
		this.entity = entity;
	}

	@Override
	public String toString()
	{
		return "InvalidFavouriteTarget [entity=" + entity + "]";
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof InvalidFavouriteTarget);

		InvalidFavouriteTarget other = (InvalidFavouriteTarget) o;
		Object entity1 = getEntity();
		Object entity2 = other.getEntity();
		if(entity1 instanceof ExpectedComparison && entity2 instanceof ExpectedComparison)
		{
			ExpectedComparison expected1 = (ExpectedComparison)entity1;
			ExpectedComparison expected2 = (ExpectedComparison)entity2;
			expected1.expected(expected2);
		}
		else
		{
			throw new RuntimeException("Entities cannot be compared");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();

		if(entity instanceof JSONAble)
		{
			JSONAble jsonAble = (JSONAble)entity;
			JSONObject entityJSON = jsonAble.toJSON();
			json.put(name, entityJSON);
		}
		else
		{
			throw new RuntimeException("Favourite target cannot be converted to JSON");
		}

		return json;
	}
	
	public String getTargetGuid()
	{
		return targetGuid;
	}
}
