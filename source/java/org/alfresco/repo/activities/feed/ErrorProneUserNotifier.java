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
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class ErrorProneUserNotifier extends AbstractUserNotifier
{
//	private AtomicInteger numSuccessful = new AtomicInteger();
//	private AtomicInteger numFailed = new AtomicInteger();

	private NodeRef failingPersonNodeRef;
	private ActionService actionService;

	public ErrorProneUserNotifier(NodeRef failingPersonNodeRef)
	{
		this.failingPersonNodeRef = failingPersonNodeRef;
	}
	
	public void setActionService(ActionService actionService)
	{
		this.actionService = actionService;
	}

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

//	public int getNumSuccess()
//	{
//		return numSuccessful.get();
//	}
//	
//	public int getNumFailed()
//	{
//		return numFailed.get();
//	}

	@Override
	protected void notifyUser(NodeRef personNodeRef, String subjectText, Object[] subjectParams,
			Map<String, Object> model, String templateNodeRef)
	{
//		super.notifyUser(personNodeRef, subjectText, model, templateNodeRef);

		String userName = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);

        Action action = actionService.createAction(ErrorProneActionExecutor.NAME);

        action.setParameterValue(ErrorProneActionExecutor.PARAM_FAILING_PERSON_NODEREF, failingPersonNodeRef);
        action.setParameterValue(ErrorProneActionExecutor.PARAM_PERSON_NODEREF, personNodeRef);
        action.setParameterValue(ErrorProneActionExecutor.PARAM_USERNAME, userName);

        actionService.executeAction(action, null);
//		String userName = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
//
//		System.out.println("userName = " + userName);
//
//		if(personNodeRef.equals(failingPersonNodeRef))
//		{
//			numFailed.incrementAndGet();
//			throw new AlfrescoRuntimeException("");
//		}
//
//		numSuccessful.incrementAndGet();
	}
}
