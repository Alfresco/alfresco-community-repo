/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.activities.feed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.json.JSONException;

/**
 * @since 4.0
 * 
 * @author Alex Miller
 */
public class DefaultActivitiesFeedModelBuilder implements ActivitiesFeedModelBuilder
{
    protected List<Map<String, Object>> activityFeedModels = new ArrayList<Map<String, Object>>();
    protected Set<String> ignoredActivityTypes = Collections.emptySet();
    
    protected long maxFeedId = -1L;

    /**
     * Set the activity types to ignore.
     */
    public void setIgnoredActivityTypes(Set<String> ignoredActivityTypes)
    {
        this.ignoredActivityTypes = ignoredActivityTypes;
    }

    @Override
    public Map<String, Object> buildModel()
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        model.put("activities", activityFeedModels);
        model.put("feedItemsCount", activityFeedModels.size());
        
        return model;
    }

    @Override
    public void addActivityFeedEntry(ActivityFeedEntity feedEntry) throws JSONException
    {
        if (ignore(feedEntry) == true) 
        {
            return;
        }
        
        this.activityFeedModels.add(feedEntry.getModel());
        
        final long feedId = feedEntry.getId();
        if (feedId > this.maxFeedId)
        {
            this.maxFeedId = feedId;
        }
    }

    @Override
    public int activityCount()
    {
        return this.activityFeedModels.size();
    }

    @Override
    public long getMaxFeedId()
    {
        return this.maxFeedId;
    }
    
    protected boolean ignore(ActivityFeedEntity feedEntry)
    {
        return this.ignoredActivityTypes.contains(feedEntry.getActivityType());
    }
}
