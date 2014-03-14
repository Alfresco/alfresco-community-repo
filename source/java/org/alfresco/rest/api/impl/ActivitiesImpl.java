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
package org.alfresco.rest.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.impl.activities.ActivitySummaryParser;
import org.alfresco.rest.api.model.Activity;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.JSONException;

/**
 * Centralises access to activities services and maps between representations.
 * 
 * @author steveglover
 *
 */
public class ActivitiesImpl implements Activities
{
	private static final String ACTIVITIES_FORMAT = "json";

	private People people;
	private ActivityService activityService;
	private ActivitySummaryParser activitySummaryParser;
    private TenantService tenantService;
    private Sites sites;
    
	public void setSites(Sites sites)
	{
		this.sites = sites;
	}

	public void setPeople(People people)
	{
		this.people = people;
	}
	
	public void setTenantService(TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	public void setActivityService(ActivityService activityService)
	{
		this.activityService = activityService;
	}

	public void setActivitySummaryParser(ActivitySummaryParser activitySummaryParser)
	{
		this.activitySummaryParser = activitySummaryParser;
	}

	public Map<String, Object> getActivitySummary(ActivityFeedEntity entity) throws JSONException
	{
		Map<String, Object> activitySummary = activitySummaryParser.parse(entity.getActivityType(), entity.getActivitySummary());
		return activitySummary;
	}
	
	private String getSiteId(String siteNetwork)
	{
		String siteId = siteNetwork;

        int idx = siteNetwork.lastIndexOf(TenantService.SEPARATOR);
        if(idx != -1)
        {
            siteId = siteNetwork.substring(idx + 1);
        }

        return siteId;
	}

    public CollectionWithPagingInfo<Activity> getUserActivities(String personId, final Parameters parameters)
    {
    	personId = people.validatePerson(personId);

    	Paging paging = parameters.getPaging();
    	String siteId = parameters.getParameter("siteId");
    	String who = parameters.getParameter("who");
    	ActivityWho activityWho = null;
    	if(who != null)
    	{
	    	try
	    	{
	    		activityWho = ActivityWho.valueOf(who);
	    	}
	    	catch(IllegalArgumentException e)
	    	{
                throw new InvalidArgumentException("Parameter who should be one of " + Arrays.toString(ActivityWho.values()));
	    	}
    	}

    	if(siteId != null && !siteId.equals(""))
    	{
    		SiteInfo siteInfo = sites.validateSite(siteId);
        	if(siteInfo == null)
        	{
        		// site does not exist
        		throw new EntityNotFoundException(siteId);
        	}
        	// set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        	siteId = siteInfo.getShortName();
    	}

    	try
    	{
            PagingResults<ActivityFeedEntity> activities = null;

            if(activityWho == null)
            {
            	activities = activityService.getPagedUserFeedEntries(personId, siteId, false, false, -1, Util.getPagingRequest(paging));
            }
            else if(activityWho.equals(ActivityWho.me))
            {
            	activities = activityService.getPagedUserFeedEntries(personId, siteId, false, true, -1, Util.getPagingRequest(paging));
            }
            else if(activityWho.equals(ActivityWho.others))
            {
            	activities = activityService.getPagedUserFeedEntries(personId, siteId, true, false, -1, Util.getPagingRequest(paging));
            }
            else
            {
                throw new InvalidArgumentException("Who argument is invalid.");
            }
            
            List<ActivityFeedEntity> feedEntities = activities.getPage();
            List<Activity> ret = new ArrayList<Activity>(feedEntities.size());
            for(ActivityFeedEntity entity : feedEntities)
            {
            	String feedSiteId = getSiteId(entity.getSiteNetwork());
            	String networkId = tenantService.getDomain(entity.getSiteNetwork());
                Activity activity = new Activity(entity.getId(), networkId, feedSiteId, entity.getFeedUserId(), entity.getPostUserId(),
                		entity.getPostDate(), entity.getActivityType(), getActivitySummary(entity));
            	ret.add(activity);
            }

            return CollectionWithPagingInfo.asPaged(paging, ret, activities.hasMoreItems(), activities.getTotalResultCount().getFirst());
    	}
    	catch(JSONException e)
    	{
    		throw new AlfrescoRuntimeException("", e);
    	}
    }
}
