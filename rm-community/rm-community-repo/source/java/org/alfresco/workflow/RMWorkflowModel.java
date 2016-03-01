 
package org.alfresco.workflow;

import org.alfresco.service.namespace.QName;

/**
 * Workflow Constants for Records Management
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public interface RMWorkflowModel
{
    // Namespace URI
    String RM_WORKFLOW_URI = "http://www.alfresco.org/model/rmworkflow/1.0";

    // Namespace prefix
    String RM_WORKFLOW_PREFIX = "rmwf";

    // Mixed Assignees
    QName RM_MIXED_ASSIGNEES = QName.createQName(RM_WORKFLOW_URI, "mixedAssignees");

    // Requested Information
    QName RM_REQUESTED_INFORMATION = QName.createQName(RM_WORKFLOW_URI, "requestedInformation");

    // Rule creator
    QName RM_RULE_CREATOR = QName.createQName(RM_WORKFLOW_URI, "ruleCreator");
}
