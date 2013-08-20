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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.rest.api.impl.activities.AbstractActivitySummaryProcessor.RemoveKey;
import org.alfresco.service.cmr.repository.NodeRef;

public class BaseActivitySummaryProcessor extends AbstractActivitySummaryProcessor
{
	@Override
	protected Change processEntry(String key, Object value)
	{
		Change change = null;

		if(key.equals("page"))
		{
			change = new ChangePageValue(key);
		}

		if(key.equals("tenantDomain"))
		{
			change = new RemoveKey(key);
		}
		
		if(key.equals("nodeRef"))
		{
			change = new ChangeKey(key, "objectId");
		}
		
		if(key.equals("parentNodeRef"))
		{
			change = new ChangeKey(key, "parentObjectId");
		}
		
		// remove null or empty properties
		if(value == null || value.equals(""))
		{
			change = new RemoveKey(key);
		}

		return change;
	}
	
	public static class ChangePageValue implements Change
	{
		private String key;
		private static final String regex = Pattern.quote("document-details?nodeRef=") + "(.*)";
		private static final Pattern pattern = Pattern.compile(regex);

		public ChangePageValue(String key) {
			super();
			this.key = key;
		}

		/*
		 * Extract and output the node id from input that looks like this: document-details?nodeRef=workspace%3A%2F%2FSpacesStore%2Fd4c1a75e-a17e-4033-94f4-988cca39a357 (non-Javadoc)
		 * 
		 * @see org.alfresco.rest.api.impl.activities.ActivitySummaryProcessor.Change#process(java.util.Map)
		 */
		public void process(Map<String, Object> entries)
		{
			String value = (String)entries.remove(key);
			try
			{
				value = URLDecoder.decode(value, "UTF-8");
				Matcher matcher = pattern.matcher(value);
				if(matcher.matches())
				{
					String nodeRefStr = matcher.group(1);
					boolean isNodeRef = NodeRef.isNodeRef(nodeRefStr);
					if(isNodeRef)
					{
						NodeRef nodeRef = new NodeRef(nodeRefStr);
						entries.put("objectId", nodeRef.getId());
					}
					else
					{
						logger.warn("Activity page url contains an invalid NodeRef " + value);
					}
				}
				else
				{
					logger.warn("Failed to match activity page url for objectId extraction " + value);
				}
			}
			catch (UnsupportedEncodingException e)
			{
				logger.warn("Unable to decode activity page url " + value);
			}
		}
	}
}
