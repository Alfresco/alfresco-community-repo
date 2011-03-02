/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Workflow Model Constants
 */
public interface WorkflowModel
{
    
    //
    // Base Business Process Management Definitions
    //

    // package folder constants
    static final QName TYPE_PACKAGE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "package");
    static final QName ASSOC_PACKAGE_CONTAINS= QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "packageContains");
    
    // task constants
    static final QName TYPE_TASK = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "task");
    static final QName PROP_TASK_ID = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "taskId");
    static final QName PROP_START_DATE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "startDate");
    static final QName PROP_DUE_DATE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "dueDate");
    static final QName PROP_COMPLETION_DATE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "completionDate");
    static final QName PROP_PRIORITY = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "priority");
    static final QName PROP_STATUS = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "status");
    static final QName PROP_PERCENT_COMPLETE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "percentComplete");
    static final QName PROP_COMPLETED_ITEMS = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "completedItems");
    static final QName PROP_COMMENT = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "comment");
    static final QName ASSOC_POOLED_ACTORS = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "pooledActors");

    // workflow task contstants
    static final QName TYPE_WORKFLOW_TASK = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowTask");
    static final QName PROP_CONTEXT = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "context");
    static final QName PROP_DESCRIPTION = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "description");
    static final QName PROP_OUTCOME = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "outcome");
    static final QName PROP_PACKAGE_ACTION_GROUP = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "packageActionGroup");
    static final QName PROP_PACKAGE_ITEM_ACTION_GROUP = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "packageItemActionGroup");
    static final QName PROP_HIDDEN_TRANSITIONS = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "hiddenTransitions");
    static final QName PROP_REASSIGNABLE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "reassignable");
    static final QName ASSOC_PACKAGE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "package");

    // Start task contstants
    static final QName TYPE_START_TASK = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "startTask");
    static final QName PROP_WORKFLOW_DESCRIPTION = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowDescription");
    static final QName PROP_WORKFLOW_PRIORITY = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowPriority");
    static final QName PROP_WORKFLOW_DUE_DATE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowDueDate");
    static final QName ASSOC_ASSIGNEE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "assignee");
    static final QName ASSOC_ASSIGNEES = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "assignees");
    static final QName ASSOC_GROUP_ASSIGNEE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "groupAssignee");
    static final QName ASSOC_GROUP_ASSIGNEES = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "groupAssignees");

    // Activiti Task Constants
    static final QName TYPE_ACTIVTI_TASK = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "activitiOutcomeTask");
    static final QName PROP_OUTCOME_PROPERTY_NAME= QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "outcomePropertyName");

    // workflow package
    static final QName ASPECT_WORKFLOW_PACKAGE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowPackage");
    static final QName PROP_IS_SYSTEM_PACKAGE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "isSystemPackage");
    static final QName PROP_WORKFLOW_DEFINITION_ID = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowDefinitionId");
    static final QName PROP_WORKFLOW_DEFINITION_NAME = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowDefinitionName");
    static final QName PROP_WORKFLOW_INSTANCE_ID = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowInstanceId");
     
    // workflow definition
    static final QName TYPE_WORKFLOW_DEF = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowDefinition");
    static final QName PROP_WORKFLOW_DEF_ENGINE_ID = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "engineId");
    static final QName PROP_WORKFLOW_DEF_NAME = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "definitionName");
    static final QName PROP_WORKFLOW_DEF_DEPLOYED = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "definitionDeployed");

}