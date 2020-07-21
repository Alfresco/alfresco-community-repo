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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.tests.PublicApiDateFormat;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Favourite implements Serializable, ExpectedComparison, Comparable<Favourite>
{
	private static final long serialVersionUID = 2812585719477560349L;

	private String username;
	private String targetGuid;
	private Date createdAt;
	private Date modifiedAt;
	private FavouritesTarget target;
	private Type type;
	private Map<String, Object> properties;

	public Favourite(FavouritesTarget target) throws ParseException
	{
		this((Date)null, (Date)null, target, null);
	}

	public Favourite(Date createdAt, Date modifiedAt, FavouritesTarget target, Map<String, Object> properties) throws ParseException
	{
		if(target != null)
		{
			this.targetGuid = target.getTargetGuid();
		}
		this.username = null;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
		this.target = target;
		this.properties = properties;
		if(target instanceof FileFavouriteTarget)
		{
			this.type = Type.FILE;
		}
		else if(target instanceof FolderFavouriteTarget)
		{
			this.type = Type.FOLDER;
		}
		else if(target instanceof SiteFavouriteTarget)
		{
			this.type = Type.SITE;
		}
		else
		{
			this.type = null;
		}
	}
	
	public Favourite(String createdAt, String modifiedAt, FavouritesTarget target, Map<String, Object> properties) throws ParseException
	{
		this(getDate(createdAt), getDate(modifiedAt), target, properties);
	}
	
	private static Date getDate(String dateStr) throws ParseException
	{
		Date date = (dateStr != null ? PublicApiDateFormat.getDateFormat().parse(dateStr) : null);
		return date;
	}

	public String getTargetGuid()
	{
		return targetGuid;
	}

	public String getUsername()
	{
		return username;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public FavouritesTarget getTarget()
	{
		return target;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public void setProperties(Map<String, Object> properties)
	{
		this.properties = properties;
	}
	
	public Date getModifiedAt()
	{
		return modifiedAt;
	}

	public Type getType()
	{
		Type type = null;
		if(target instanceof FileFavouriteTarget)
		{
			type = Type.FILE;
		}
		else if(target instanceof FolderFavouriteTarget)
		{
			type = Type.FOLDER;
		}
		else if(target instanceof SiteFavouriteTarget)
		{
			type = Type.SITE;
		}
		return type;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject favouriteJson = new JSONObject();
		if(target != null)
		{
			favouriteJson.put("target", target.toJSON());
		}
		return favouriteJson;
	}

	public static FavouritesTarget parseTarget(JSONObject jsonObject) throws ParseException
	{
		FavouritesTarget ret = null;

		if(jsonObject.containsKey("site"))
		{
			JSONObject siteJSON = (JSONObject)jsonObject.get("site");
			Site site = SiteImpl.parseSite(siteJSON);
			ret = new SiteFavouriteTarget(site);
			
		}
		else if(jsonObject.containsKey("file"))
		{
			JSONObject documentJSON = (JSONObject)jsonObject.get("file");
			FavouriteDocument document = FavouriteDocument.parseDocument(documentJSON);
			ret = new FileFavouriteTarget(document);
			
		}
		else if(jsonObject.containsKey("folder"))
		{
			JSONObject folderJSON = (JSONObject)jsonObject.get("folder");
			FavouriteFolder folder = FavouriteFolder.parseFolder(folderJSON);
			ret = new FolderFavouriteTarget(folder);
		}

		return ret;
	}

	public static Favourite parseFavourite(JSONObject jsonObject) throws ParseException
	{
		String createdAt = (String)jsonObject.get("createdAt");
		String modifiedAt = (String)jsonObject.get("modifiedAt");
		JSONObject jsonTarget = (JSONObject)jsonObject.get("target");
		Map properties = null;
		try
		{
			properties = RestApiUtil.parsePojo("properties", jsonObject, Map.class);
		}
		catch (Exception e)
		{
			// ignore
		}
		FavouritesTarget target = parseTarget(jsonTarget);
		Favourite favourite = new Favourite(createdAt, modifiedAt, target, properties);
		return favourite;
	}

	public static ListResponse<Favourite> parseFavourites(JSONObject jsonObject) throws ParseException
	{
		List<Favourite> favourites = new ArrayList<Favourite>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			favourites.add(Favourite.parseFavourite(entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
		return new ListResponse<Favourite>(paging, favourites);
	}
	
	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof Favourite);
		
		Favourite other = (Favourite)o;

		if(target == null)
		{
			fail();
		}
		target.expected(other.getTarget());
		if(createdAt != null)
		{
			assertTrue(other.getCreatedAt().equals(createdAt) || other.getCreatedAt().after(createdAt));
		}
		if(modifiedAt != null)
		{
			AssertUtil.assertEquals("modifiedAt", modifiedAt, other.getModifiedAt());
		}
		AssertUtil.assertEquals("targetGuid", targetGuid, other.getTargetGuid());
	}

	@Override
	public String toString()
	{
		return "Favourite [username=" + username + ", targetGuid=" + targetGuid
				+ ", createdAt=" + createdAt + ", modifiedAt = " + modifiedAt + ", target=" + target + "]";
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((targetGuid == null) ? 0 : targetGuid.hashCode());
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
		Favourite other = (Favourite) obj;
		if (targetGuid == null) {
			if (other.targetGuid != null)
				return false;
		} else if (!targetGuid.equals(other.targetGuid))
			return false;
		return true;
	}

	@Override
	public int compareTo(Favourite o)
	{
		int idx = (type != null ? type.compareTo(o.getType()) : 0);
		if(idx == 0)
		{
			idx = o.getCreatedAt().compareTo(createdAt);			
		}
		
		return idx;
	}
}
