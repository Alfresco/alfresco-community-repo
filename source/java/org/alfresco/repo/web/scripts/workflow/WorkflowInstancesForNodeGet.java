/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
