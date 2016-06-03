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
