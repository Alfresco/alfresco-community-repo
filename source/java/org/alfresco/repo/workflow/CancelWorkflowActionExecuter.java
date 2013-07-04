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
package org.alfresco.repo.workflow;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dward
 */
public class CancelWorkflowActionExecuter extends ActionExecuterAbstractBase
{
    protected static Log log = LogFactory.getLog(CancelWorkflowActionExecuter.class);

    public static String NAME = "cancel-workflow";
    
    public static final String PARAM_WORKFLOW_ID_LIST = "workflow-id-list";   // list of workflow IDs
    
    private WorkflowService workflowService;
    
    /**
     * @param workflowService the workflowService to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        @SuppressWarnings("unchecked")
        List<String> workflowIds = (List<String>) action.getParameterValue(PARAM_WORKFLOW_ID_LIST);
        
        if (log.isTraceEnabled()) { log.trace("Cancelling " + (workflowIds == null ? 0 : workflowIds.size()) + " workflows by ID."); }
        
        if (workflowIds != null && !workflowIds.isEmpty())
        {
            this.workflowService.cancelWorkflows(workflowIds);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(
                new ParameterDefinitionImpl(PARAM_WORKFLOW_ID_LIST,
                                            DataTypeDefinition.ANY,
                                            false,
                                            getParamDisplayLabel(PARAM_WORKFLOW_ID_LIST)));
    }

}
