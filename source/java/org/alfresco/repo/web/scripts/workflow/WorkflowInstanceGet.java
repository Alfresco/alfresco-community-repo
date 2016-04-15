package org.alfresco.repo.web.scripts.workflow;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 * @since 3.4
 */
public class WorkflowInstanceGet extends AbstractWorkflowWebscript
{
    public static final String PARAM_INCLUDE_TASKS = "includeTasks";

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // getting workflow instance id from request parameters
        String workflowInstanceId = params.get("workflow_instance_id");

        boolean includeTasks = getIncludeTasks(req);

        WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowInstanceId);

        // task was not found -> return 404
        if (workflowInstance == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find workflow instance with id: " + workflowInstanceId);
        }

        Map<String, Object> model = new HashMap<String, Object>();
        // build the model for ftl
        model.put("workflowInstance", modelBuilder.buildDetailed(workflowInstance, includeTasks));

        return model;
    }

    private boolean getIncludeTasks(WebScriptRequest req)
    {
        String includeTasks = req.getParameter(PARAM_INCLUDE_TASKS);
        if (includeTasks != null)
        {
            try
            {
                return Boolean.valueOf(includeTasks);
            }
            catch (Exception e)
            {
                // do nothing, false will be returned
            }
        }

        // Defaults to false.
        return false;
    }

}
