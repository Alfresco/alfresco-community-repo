package org.alfresco.module.org_alfresco_module_rm.action;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Record Management Action
 *
 * @author Roy Wetherall
 */
public interface RecordsManagementAction
{
    /**
     * Get the name of the action
     *
     * @return  String  action name
     */
    String getName();

    /**
     * Get the label of the action
     *
     * @return  String  action label
     */
    String getLabel();

    /**
     * Get the description of the action
     *
     * @return  String  action description
     */
    String getDescription();

    /**
     * Indicates whether this is a disposition action or not
     *
     * @return  boolean     true if a disposition action, false otherwise
     */
    boolean isDispositionAction();

    /**
     * Execution of the action
     *
     * @param filePlanComponent     file plan component the action is executed upon
     * @param parameters            action parameters
     */
    RecordsManagementActionResult execute(NodeRef filePlanComponent, Map<String, Serializable> parameters);

    /**
     * Some admin-related rmActions execute against a target nodeRef which is not provided
     * by the calling code, but is instead an implementation detail of the action.
     *
     * @return the target nodeRef
     */
    NodeRef getImplicitTargetNodeRef();

    /**
     * Get the records management action definition.
     *
     * @return
     * @since 2.1
     */
    RecordsManagementActionDefinition getRecordsManagementActionDefinition();

    /**
     * Indicates whether the action is public or not
     *
     * @return
     * @since 2.1
     */
    boolean isPublicAction();
}
