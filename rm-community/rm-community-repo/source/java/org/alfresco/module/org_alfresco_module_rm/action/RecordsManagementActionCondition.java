 
package org.alfresco.module.org_alfresco_module_rm.action;

/**
 * Record Management Action Condition
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface RecordsManagementActionCondition
{
    /**
     * Get the name of the action condition
     *
     * @return  String  action condition name
     */
    String getName();

    /**
     * Get the label of the action condition
     *
     * @return  String  action condition label
     */
    String getLabel();

    /**
     * Get the description of the action condition
     *
     * @return  String  action condition description
     */
    String getDescription();

    /**
     *
     * @return
     */
    RecordsManagementActionConditionDefinition getRecordsManagementActionConditionDefinition();

    /**
     *
     * @return
     */
    boolean isPublicCondition();
}
