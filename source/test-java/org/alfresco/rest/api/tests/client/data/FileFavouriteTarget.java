package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;

public class FileFavouriteTarget implements FavouritesTarget
{
	private Document document;

	public FileFavouriteTarget(Document document)
	{
		super();
		this.document = document;
	}

	public Document getDocument()
	{
		return document;
	}

	@Override
	public String toString()
	{
		return "FileFavouriteTarget [document=" + document + "]";
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJSON()
	{
		JSONObject favouriteJson = new JSONObject();
		favouriteJson.put("file", getDocument().toJSON());
		return favouriteJson;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof FileFavouriteTarget);
		
		FileFavouriteTarget other = (FileFavouriteTarget)o;
		
		document.expected(other.getDocument());
	}
	
	public String getTargetGuid()
	{
		return document.getGuid();
	}
}
