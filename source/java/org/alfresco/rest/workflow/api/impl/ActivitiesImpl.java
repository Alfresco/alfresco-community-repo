package org.alfresco.rest.workflow.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Activities;
import org.alfresco.rest.workflow.api.model.Activity;

public class ActivitiesImpl extends WorkflowRestImpl implements Activities
{
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_COMPLETED = "completed";

    @Override
    public CollectionWithPagingInfo<Activity> getActivities(String processId, Parameters parameters)
    {
        Paging paging = parameters.getPaging();
        String status = parameters.getParameter("status");
        
        validateIfUserAllowedToWorkWithProcess(processId);

        HistoricActivityInstanceQuery query = activitiProcessEngine
                .getHistoryService()
                .createHistoricActivityInstanceQuery();
        
        if (STATUS_ACTIVE.equals(status)) query.unfinished();
        else if (STATUS_COMPLETED.equals(status)) query.finished();
        
        query.processInstanceId(processId);
        
        query.orderByExecutionId().asc();
        
        List<HistoricActivityInstance> activities = query.listPage(paging.getSkipCount(), paging.getMaxItems());

        List<Activity> page = new ArrayList<Activity>(activities.size());
        for (HistoricActivityInstance activityInstance: activities) 
        {
            Activity activity = new Activity(activityInstance);
            page.add(activity);
        }
        
        return CollectionWithPagingInfo.asPaged(paging, page, false, page.size());
    }

}
