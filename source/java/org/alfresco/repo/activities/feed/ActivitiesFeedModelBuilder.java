package org.alfresco.repo.activities.feed;

import java.util.Map;

import org.json.JSONException;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;

/**
 * Builder interface for creating the model passed to activities feed email templates. 
 *
 * @author Alex Miller
 */
public interface ActivitiesFeedModelBuilder
{

    Map<String, Object> buildModel();

    void addActivityFeedEntry(ActivityFeedEntity feedEntry) throws JSONException;

    int activityCount();

    long getMaxFeedId();

}
