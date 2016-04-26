package org.alfresco.service.cmr.action;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Rule condition interface
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ActionCondition extends ParameterizedItem
{
	/**
	 * Get the action condition definition name
	 */
    public String getActionConditionDefinitionName();
    
    /**
     * Set whether the condition result should be inverted.
     * <p>
     * This is achieved by applying the NOT logical operator to the
     * result.
     * <p>
     * The default value is false.
     * 
     * @param invertCondition   true indicates that the result of the condition
     *                          is inverted, false otherwise.
     */
    public void setInvertCondition(boolean invertCondition);
    
    /**
     * Indicates whether the condition result should be inverted.
     * <p>
     * This is achieved by applying the NOT logical operator to the result.
     * <p>
     * The default value is false.
     * 
     * @return  true indicates that the result of the condition is inverted, false 
     *          otherwise
     */
    public boolean getInvertCondition();
}
