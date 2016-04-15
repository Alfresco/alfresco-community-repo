/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.activities.feed;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * A test user notifier.
 *
 * @since 4.0
 */
public class MockUserNotifier extends AbstractUserNotifier
{
    /**
     * Default alfresco installation url
     */
	private BitSet notifiedPersonsTracker = new BitSet();
    private AtomicInteger count = new AtomicInteger(0);

	@Override
	protected boolean skipUser(NodeRef personNodeRef)
	{
		return false;
	}

	@Override
	protected Long getFeedId(NodeRef personNodeRef)
	{
		Map<QName, Serializable> personProps = nodeService.getProperties(personNodeRef);

		// where did we get up to ?
		Long emailFeedDBID = (Long)personProps.get(ContentModel.PROP_EMAIL_FEED_ID);
		if (emailFeedDBID != null)
		{
			// increment min feed id
			emailFeedDBID++;
		}
		else
		{
			emailFeedDBID = -1L;
		}
		
		return emailFeedDBID;
	}

	@Override
	protected void notifyUser(NodeRef personNodeRef, String subjectText, Object[] subjectParams, Map<String, Object> model, String templateNodeRef)
	{
		String username = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
		if(username.startsWith("user"))
		{
			int id = Integer.parseInt(username.substring(4));

			boolean b = false;
			synchronized(notifiedPersonsTracker)
			{
				b = notifiedPersonsTracker.get(id);
			}
			if(b)
			{
				System.out.println("Already set: " + id);
			}
			else
			{
				synchronized(notifiedPersonsTracker)
				{
					notifiedPersonsTracker.set(id);
				}
			}
		}

		count.incrementAndGet();
	}
	
	public int countNotifications()
	{
		return count.get();		
	}
	
	public int nextUserId()
	{
		synchronized(notifiedPersonsTracker)
		{
			return notifiedPersonsTracker.nextClearBit(1);
		}
	}
}
