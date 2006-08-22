/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
    static final QName ASSOC_POOLED_ACTORS = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "pooledActors");

    // workflow task contstants
    static final QName TYPE_WORKFLOW_TASK = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowTask");
    static final QName PROP_CONTEXT = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "context");
    static final QName PROP_OUTCOME = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "outcome");
    static final QName PROP_PACKAGE_ACTION_GROUP = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "packageActionGroup");
    static final QName PROP_PACKAGE_ITEM_ACTION_GROUP = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "packageItemActionGroup");
    static final QName ASSOC_PACKAGE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "package");
    
    // workflow package
    static final QName ASPECT_WORKFLOW_PACKAGE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowPackage");
    static final QName PROP_WORKFLOW_DEFINITION_ID = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowDefinitionId");
    static final QName PROP_WORKFLOW_DEFINITION_NAME = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowDefinitionName");
    static final QName PROP_WORKFLOW_INSTANCE_ID = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "workflowInstanceId");
 

    //
    // Workflow Models
    //
    
    // review & approve
    static final QName TYPE_SUBMITREVIEW_TASK = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "submitReviewTask");
    static final QName PROP_REVIEW_PRIORITY = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewPriority");
    static final QName PROP_REVIEW_DUE_DATE = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewDueDate");
    static final QName ASSOC_REVIEWER = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewer");
    
}