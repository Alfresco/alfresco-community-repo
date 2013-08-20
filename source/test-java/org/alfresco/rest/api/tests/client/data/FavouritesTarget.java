package org.alfresco.rest.api.tests.client.data;

import org.json.simple.JSONObject;

public interface FavouritesTarget extends ExpectedComparison
{
	JSONObject toJSON();
	String getTargetGuid();
}
