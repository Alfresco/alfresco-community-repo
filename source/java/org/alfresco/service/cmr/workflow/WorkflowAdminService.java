package org.alfresco.service.cmr.workflow;

/**
 * Client facing API for providing administration information about the
 * {@link WorkflowService}.
 *
 * @author Gavin Cornwell
 * @since 4.0
 */
public interface WorkflowAdminService
{
    /**
     * Determines whether the engine with the given id is enabled.
     * 
     * @param engineId The id of a workflow engine
     * @return true if the engine id is valid and is enabled
     */
    boolean isEngineEnabled(String engineId);

    /**
     * Enables/disables the engine with the given id.
     * 
     * @param engineId The id of a workflow engine
     * @param isEnabled true to enable the engine, false to disable
     */
    public void setEngineEnabled(String engineId, boolean isEnabled);

    /**
     * Determines whether the workflow definitions are visible
     * for the engine with the given id.
     * 
     * NOTE: Workflow definitions can always be retrieved directly 
     * i.e. via name or id
     * 
     * @param engineId The id of a workflow engine
     * @return true if the definitions are visible
     */
    boolean isEngineVisible(String engineId);
    
    /**
     * Sets the visiblity of workflow definitions
     * for the engine with the given id.
     * 
     * NOTE: Workflow definitions can always be retrieved directly 
     * i.e. via name or id
     * 
     * @param engineId The id of a workflow engine
     * @param isVisible true if the definitions are visible
     */
    public void setEngineVisibility(String engineId, boolean isVisible);
}
