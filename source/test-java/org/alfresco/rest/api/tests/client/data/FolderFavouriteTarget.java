package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;

public class FolderFavouriteTarget implements FavouritesTarget
{
	private Folder folder;

	public FolderFavouriteTarget(Folder folder)
	{
		super();
		this.folder = folder;
	}

	public Folder getFolder()
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
