package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.List;

import org.alfresco.rest.core.RestModels;
import org.alfresco.utility.model.ProcessModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;

/**
 * Handle collection of <RestTaskModel>
 * 
 * @author Critina Axinte
 */
public class RestTaskModelsCollection extends RestModels<RestTaskModel, RestTaskModelsCollection>
{    
    public RestTaskModel getTaskModelByAssignee(UserModel assigneeName)
    {
        List<RestTaskModel> tasksList = getEntries();

        for (RestTaskModel taskModel: tasksList)
        {
            if (taskModel.onModel().getAssignee().equals(assigneeName.getUsername()))
            {
                STEP(String.format("REST API: Get task with assignee '%s'", assigneeName.getUsername()));
                return taskModel.onModel();
            }
        }

        return null;
    }
    
    public RestTaskModel getTaskModelByDescription(SiteModel siteModel)
    {
        List<RestTaskModel> tasksList = getEntries();

        for (RestTaskModel taskModel: tasksList)
        {
            if (taskModel.onModel().getDescription().equals(String.format("Request to join %s site", siteModel.getId())))
            {
                STEP(String.format("REST API: Get task with site name '%s'", siteModel.getId()));
                return taskModel.onModel();
            }
        }

        return null;
    }
    
    public RestTaskModel getTaskModelByProcess(ProcessModel process)
    {
        List<RestTaskModel> tasksList = getEntries();

        for (RestTaskModel taskModel: tasksList)
        {
            if (taskModel.onModel().getProcessId().equals(process.getId()))
            {
                STEP(String.format("REST API: Get task with processId '%s'", process.getId()));
                return taskModel.onModel();
            }
        }

        return null;
    }
}    