package org.alfresco.repo.web.scripts.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 * @since 3.4
 */
public class WorkflowInstancesForNodeGet extends AbstractWorkflowWebscript
{

    public static final String PARAM_STORE_TYPE = "store_type";
    public static final String PARAM_STORE_ID = "store_id";
    public static final String PARAM_NODE_ID = "id";

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // get nodeRef from request
        NodeRef nodeRef = new NodeRef(params.get(PARAM_STORE_TYPE), params.get(PARAM_STORE_ID), params.get(PARAM_NODE_ID));

        // list all active workflows for nodeRef
        List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(nodeRef, true);
        
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(workflows.size());

        for (WorkflowInstance workflow : workflows)
        {
            results.add(modelBuilder.buildSimple(workflow));
        }

        Map<String, Object> model = new HashMap<String, Object>();
        // build the model for ftl
        model.put("workflowInstances", results);

        return model;
    }
}
