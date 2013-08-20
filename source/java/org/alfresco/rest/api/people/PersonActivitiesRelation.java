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
package org.alfresco.rest.api.people;

import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.model.Activity;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter.KIND;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author steveglover
 *
 */
@RelationshipResource(name = "activities", entityResource = PeopleEntityResource.class, title = "Person Activities")
public class PersonActivitiesRelation implements RelationshipResourceAction.Read<Activity>, InitializingBean
{
    private static final Log logger = LogFactory.getLog(PersonActivitiesRelation.class);

    private Activities activities;

	public void setActivities(Activities activities)
	{
		this.activities = activities;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("activities", this.activities);
    }

    /*
     * List the user's activities, excluding those of other users.
     * 
     * /people/[id]/activities
     * 
     * ordered by postDate
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Get#get(java.io.Serializable)
     */
    @Override
    @WebApiDescription(title = "List the user's activities, excluding those of other users.")
    @WebApiParameters({
    		@WebApiParam(name = "who", title = "Who",
    		            description="Filter to include the user's activities only ('me'), other user's activities only ('others'), or all activities (don't include the parameter).", kind=KIND.QUERY_STRING),
    		@WebApiParam(name = "siteId", title="siteId", description = "Include only activity feed entries relating to this site.", kind=KIND.QUERY_STRING)
    })
    public CollectionWithPagingInfo<Activity> readAll(String personId, Parameters parameters)
    {
    	return activities.getUserActivities(personId, parameters);
    }
	
}
