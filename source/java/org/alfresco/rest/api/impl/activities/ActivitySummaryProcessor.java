package org.alfresco.rest.api.impl.activities;

import java.util.Map;

public interface ActivitySummaryProcessor
{
	public interface Change
	{
		void process(Map<String, Object> entries);
	}

	public Map<String, Object> process(Map<String, Object> entries);
}
