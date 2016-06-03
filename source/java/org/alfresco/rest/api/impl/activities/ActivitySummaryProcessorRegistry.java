package org.alfresco.rest.api.impl.activities;

/**
 * A registry for activity summary parsers/post processors.
 * 
 * @author steveglover
 *
 */
public interface ActivitySummaryProcessorRegistry
{
	public void register(String activityType, ActivitySummaryProcessor processor);
}
