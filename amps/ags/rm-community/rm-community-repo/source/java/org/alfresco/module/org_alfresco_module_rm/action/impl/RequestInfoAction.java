/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.workflow.RMWorkflowModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Request info action for starting a workflow to request more information for an undeclared record
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RequestInfoAction extends RMActionExecuterAbstractBase
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RequestInfoAction.class);

    /** Parameter names */
    public static final String PARAM_REQUESTED_INFO = "requestedInfo";
    public static final String PARAM_ASSIGNEES = "assignees";
    public static final String PARAM_RULE_CREATOR = "ruleCreator";

    /** Action name */
    public static final String NAME = "requestInfo";

    /** Workflow definition name */
    private static final String REQUEST_INFO_WORKFLOW_DEFINITION_NAME = "activiti$activitiRequestForInformation";

    /** Workflow service */
    private WorkflowService workflowService;

    /**
     * @param workflowService   workflow service
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (getNodeService().exists(actionedUponNodeRef) &&
            !getNodeService().hasAspect(actionedUponNodeRef, ContentModel.ASPECT_PENDING_DELETE) &&
            getRecordService().isRecord(actionedUponNodeRef) &&
            !getRecordService().isDeclared(actionedUponNodeRef))
        {
            String workflowDefinitionId = workflowService.getDefinitionByName(REQUEST_INFO_WORKFLOW_DEFINITION_NAME).getId();
            Map<QName, Serializable> parameters = new HashMap<>();

            parameters.put(WorkflowModel.ASSOC_PACKAGE, getWorkflowPackage(action, actionedUponNodeRef));
            parameters.put(RMWorkflowModel.RM_MIXED_ASSIGNEES, getAssignees(action));
            parameters.put(RMWorkflowModel.RM_REQUESTED_INFORMATION, getRequestedInformation(action));
            parameters.put(RMWorkflowModel.RM_RULE_CREATOR, getRuleCreator(action));

            workflowService.startWorkflow(workflowDefinitionId, parameters);
        }
        else
        {
            logger.info("Can't start the request information workflow for node '" + actionedUponNodeRef.toString() + "'.");
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_REQUESTED_INFO, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_REQUESTED_INFO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_ASSIGNEES, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_ASSIGNEES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_RULE_CREATOR, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_RULE_CREATOR)));
    }

    /**
     * Helper method for creating a workflow package to contain the actioned upon nodeRef
     *
     * @param action The request info action
     * @param actionedUponNodeRef The actioned upon nodeRef
     * @return Returns a workflow package containing the actioned upon nodeRef
     */
    private NodeRef getWorkflowPackage(Action action, NodeRef actionedUponNodeRef)
    {
        NodeRef workflowPackage = (NodeRef) action.getParameterValue(WorkflowModel.ASSOC_PACKAGE.toPrefixString(getNamespaceService()));
        workflowPackage = workflowService.createPackage(workflowPackage);
        ChildAssociationRef childAssoc = getNodeService().getPrimaryParent(actionedUponNodeRef);
        getNodeService().addChild(workflowPackage, actionedUponNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, childAssoc.getQName());
        return workflowPackage;
    }

    /**
     * Helper method for getting the assignees from the action
     *
     * @param action The request info action
     * @return Returns a list of {@link NodeRef}s each representing the assignee
     */
    private Serializable getAssignees(Action action)
    {
        List<NodeRef> assigneesList = new ArrayList<>();
        String assigneesAsString = (String) action.getParameterValue(PARAM_ASSIGNEES);
        String[] assignees = StringUtils.split(assigneesAsString, ',');
        for (String assignee : assignees)
        {
            assigneesList.add(new NodeRef(assignee));
        }
        return (Serializable) assigneesList;
    }

    /**
     * Helper method for getting the requested information from the action
     *
     * @param action The request info action
     * @return Returns the requested information
     */
    private Serializable getRequestedInformation(Action action)
    {
        return action.getParameterValue(PARAM_REQUESTED_INFO);
    }

    /**
     * Helper method for getting the rule creator
     *
     * @param action The request info action
     * @return Returns the rule creator
     */
    private Serializable getRuleCreator(Action action)
    {
        return action.getParameterValue(PARAM_RULE_CREATOR);
    }

}
