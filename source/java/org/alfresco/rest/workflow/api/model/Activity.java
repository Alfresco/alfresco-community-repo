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
package org.alfresco.rest.workflow.api.model;

import java.util.Date;

import org.activiti.engine.history.HistoricActivityInstance;

/**
 * Representation of an activity in the Activiti engine.
 * 
 * @author Tijs Rademakers
 */
public class Activity
{
    String id;
    String activityDefinitionId;
    String activityDefinitionName;
    String activityDefinitionType;
    Date startedAt;
    Date endedAt;
    Long durationInMs;
    
    public Activity(HistoricActivityInstance activity) {
        this.id = activity.getId();
        this.activityDefinitionId = activity.getActivityId();
        this.activityDefinitionName = activity.getActivityName();
        this.activityDefinitionType = activity.getActivityType();
        this.startedAt = activity.getStartTime();
        this.endedAt = activity.getEndTime();
        this.durationInMs = activity.getDurationInMillis();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getActivityDefinitionId()
    {
        return activityDefinitionId;
    }

    public void setActivityDefinitionId(String activityDefinitionId)
    {
        this.activityDefinitionId = activityDefinitionId;
    }

    public String getActivityDefinitionName()
    {
        return activityDefinitionName;
    }

    public void setActivityDefinitionName(String activityDefinitionName)
    {
        this.activityDefinitionName = activityDefinitionName;
    }

    public String getActivityDefinitionType()
    {
        return activityDefinitionType;
    }

    public void setActivityDefinitionType(String activityDefinitionType)
    {
        this.activityDefinitionType = activityDefinitionType;
    }

    public Date getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(Date startedAt)
    {
        this.startedAt = startedAt;
    }

    public Date getEndedAt()
    {
        return endedAt;
    }

    public void setEndedAt(Date endedAt)
    {
        this.endedAt = endedAt;
    }

    public Long getDurationInMs()
    {
        return durationInMs;
    }

    public void setDurationInMs(Long durationInMs)
    {
        this.durationInMs = durationInMs;
    }
}
