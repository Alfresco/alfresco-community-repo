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
package org.alfresco.rest.workflow.api.processes;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Activities;
import org.alfresco.rest.workflow.api.model.Activity;

/**
 * 
 * @author Tijs Rademakers
 *
 */
@RelationshipResource(name = "activities", entityResource = ProcessesRestEntityResource.class, title = "Activities for the current process")
public class ProcessActivitiesRelation implements RelationshipResourceAction.Read<Activity>
{
    protected Activities activities;
    
    public void setActivities(Activities activities)
    {
        this.activities = activities;
    }

    /**
     * List the activities.
     */
    @Override
    @WebApiDescription(title = "Get Activities", description = "Get a paged list of the activities")
    public CollectionWithPagingInfo<Activity> readAll(String processId, Parameters parameters)
    {
        return activities.getActivities(processId, parameters);
    }
}
