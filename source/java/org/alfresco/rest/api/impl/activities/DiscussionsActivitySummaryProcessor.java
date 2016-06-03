package org.alfresco.rest.api.impl.activities;

public class DiscussionsActivitySummaryProcessor extends BaseActivitySummaryProcessor
{
	@Override
	protected Change processEntry(String key, Object value)
	{
		Change change = null;

		if(key.equals("params"))
		{
			change = new RemoveKey(key);
		}
		
		return change;
	}
}
