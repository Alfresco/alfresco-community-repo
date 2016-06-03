package org.alfresco.repo.action;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.action.ActionCondition;

/**
 * @author Roy Wetherall
 */
public class ActionConditionImpl extends ParameterizedItemImpl implements Serializable,
        ActionCondition
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3257288015402644020L;
    
    /**
     * Rule condition definition
     */
    private String actionConditionDefinitionName;
    
    /**
     * Indicates whether the result of the condition should have the NOT logical operator applied 
     * to it.
     */
    private boolean invertCondition = false;

    /**
     * Constructor
     */
    public ActionConditionImpl(String id, String actionConditionDefinitionName)
    {
        this(id, actionConditionDefinitionName, null);
    }

    /**
     *
     * @param id String
     * @param actionConditionDefinitionName String
     * @param parameterValues Map<String, Serializable>
     */
    public ActionConditionImpl(
            String id,
            String actionConditionDefinitionName, 
            Map<String, Serializable> parameterValues)
    {
        super(id, parameterValues);
        this.actionConditionDefinitionName = actionConditionDefinitionName;
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionCondition#getActionConditionDefinitionName()
     */
    public String getActionConditionDefinitionName()
    {
        return this.actionConditionDefinitionName;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ActionCondition#setInvertCondition(boolean)
     */
    public void setInvertCondition(boolean invertCondition)
    {
        this.invertCondition = invertCondition;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ActionCondition#getInvertCondition()
     */
    public boolean getInvertCondition()
    {
        return this.invertCondition;
    }    
}
