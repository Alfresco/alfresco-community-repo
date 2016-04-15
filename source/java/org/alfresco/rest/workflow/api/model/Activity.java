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
