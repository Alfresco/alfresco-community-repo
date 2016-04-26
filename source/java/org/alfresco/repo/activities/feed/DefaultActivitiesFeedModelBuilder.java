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
