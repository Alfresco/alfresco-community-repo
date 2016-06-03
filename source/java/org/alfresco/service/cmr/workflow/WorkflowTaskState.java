package org.alfresco.service.cmr.workflow;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Workflow Task State
 * 
 * Represents the high-level state of Workflow Task (in relation to "in-flight" 
 * workflow instance).
 *
 * A user-defined task state may be represented as Task Property (and described
 * by the Alfresco Data Dictionary).
 * 
 * @author davidc
 */
@AlfrescoPublicApi
public enum WorkflowTaskState
{
    IN_PROGRESS,
    COMPLETED;
}
