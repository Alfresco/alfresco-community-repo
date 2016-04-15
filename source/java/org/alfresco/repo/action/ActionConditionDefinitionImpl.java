package org.alfresco.repo.action;

import org.alfresco.service.cmr.action.ActionConditionDefinition;

/**
 * Rule condition implementation class.
 * 
 * @author Roy Wetherall
 */
public class ActionConditionDefinitionImpl extends ParameterizedItemDefinitionImpl 
                               implements ActionConditionDefinition
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3688505493618177331L;

    /**
     * ActionCondition evaluator
     */
    private String conditionEvaluator;
    
    /**
     * Constructor
     * 
     * @param name                  the name
     */
    public ActionConditionDefinitionImpl(String name)
    {
        super(name);
    }

    /**
     * Set the condition evaluator
     * 
     * @param conditionEvaluator  the condition evaluator
     */
    public void setConditionEvaluator(String conditionEvaluator)
    {
        this.conditionEvaluator = conditionEvaluator;
    }
    
    /**
     * Get the condition evaluator
     * 
     * @return  the condition evaluator
     */
    public String getConditionEvaluator()
    {
        return conditionEvaluator;
    }
}
