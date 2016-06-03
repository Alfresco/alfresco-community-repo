package org.alfresco.rest.api;

import java.util.Map;

import org.alfresco.repo.Client;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.rest.api.model.Activity;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.json.JSONException;

public interface Activities
{
	static final String APP_TOOL = "restapi";
	static final Client RESTAPI_CLIENT = Client.asType(Client.ClientType.restapi);

	public static enum ActivityWho
	{
		me, others;
	};

	public Map<String, Object> getActivitySummary(ActivityFeedEntity entity) throws JSONException;
    public CollectionWithPagingInfo<Activity> getUserActivities(String personId, final Parameters parameters);
}
