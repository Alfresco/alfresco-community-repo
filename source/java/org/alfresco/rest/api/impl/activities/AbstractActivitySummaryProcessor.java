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
package org.alfresco.rest.api.impl.activities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

public abstract class AbstractActivitySummaryProcessor extends AbstractLifecycleBean implements ActivitySummaryProcessor
{
	protected static Log logger = LogFactory.getLog(ActivitySummaryProcessor.class);  

	protected ActivitySummaryProcessorRegistry registry;
    private List<String> eventTypes;

	public void setEventTypes(List<String> eventTypes)
	{
		this.eventTypes = eventTypes;
	}

	public void setRegistry(ActivitySummaryProcessorRegistry registry)
	{
		this.registry = registry;
	}

    public void setCustomRenditions(List<String> eventTypes)
    {
    	this.eventTypes = eventTypes;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    protected void onBootstrap(ApplicationEvent event)
    {
    	register();
    }
    
    protected void onShutdown(ApplicationEvent event)
    {
    	
    }
    
	@Override
	public Map<String, Object> process(Map<String, Object> entries)
	{
		List<Change> changes = new LinkedList<Change>();
		Map<String, Object> ret = new HashMap<String, Object>(entries.size());
		for(Map.Entry<String, Object> entry : entries.entrySet())
		{
			String key = entry.getKey();
			Object value = entry.getValue();
			Change change = processEntry(key, value);
			if(change != null)
			{
				changes.add(change);
			}
		}

		for(Change change : changes)
		{
			if(change != null)
			{
				change.process(entries);
			}			
		}

		return ret;
	}
	
	protected abstract Change processEntry(String key, Object value);

    protected void register()
    {
    	for(String eventType : eventTypes)
    	{
    		registry.register(eventType, this);
    	}
    }

	public static class ChangeKey implements Change
	{
		private String oldKey;
		private String newKey;

		public ChangeKey(String oldKey, String newKey) {
			super();
			this.oldKey = oldKey;
			this.newKey = newKey;
		}

		public void process(Map<String, Object> entries)
		{
			Object value = entries.remove(oldKey);
			entries.put(newKey, value);
		}
	}

	public static class RemoveKey implements Change
	{
		private String key;

		public RemoveKey(String key) {
			super();
			this.key = key;
		}

		public void process(Map<String, Object> entries)
		{
			entries.remove(key);
		}
	}
    
}
