package org.alfresco.rest.workflow.api;

import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.model.Activity;

public interface Activities
{
    CollectionWithPagingInfo<Activity> getActivities(String processId, Parameters parameters);
}
