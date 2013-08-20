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
package org.alfresco.rest.api;

import java.util.Map;

import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.rest.api.model.Activity;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.json.JSONException;

public interface Activities
{
	public static enum ActivityWho
	{
		me, others;
	};

	public Map<String, Object> getActivitySummary(ActivityFeedEntity entity) throws JSONException;
    public CollectionWithPagingInfo<Activity> getUserActivities(String personId, final Parameters parameters);
}
