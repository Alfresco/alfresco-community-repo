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

public class FolderFavouriteTarget implements FavouritesTarget
{
	private FavouriteFolder folder;

	public FolderFavouriteTarget(FavouriteFolder folder)
	{
		super();
		this.folder = folder;
	}

	public FavouriteFolder getFolder()
	{
		return folder;
	}

	@Override
	public String toString()
	{
		return "FolderFavouriteTarget [folder=" + folder + "]";
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		json.put("folder", getFolder().toJSON());
		return json;
	}
	
	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof FolderFavouriteTarget);
		
		FolderFavouriteTarget other = (FolderFavouriteTarget)o;
		
		folder.expected(other.getFolder());
	}

	public String getTargetGuid()
	{
		return folder.getGuid();
	}
}
