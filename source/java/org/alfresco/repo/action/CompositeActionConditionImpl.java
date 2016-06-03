package org.alfresco.repo.action;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeActionCondition;

/**
 * Composite action condition implementation
 * 
 * @author Jean Barmash
 */

public class CompositeActionConditionImpl extends ActionConditionImpl implements CompositeActionCondition 
{

    public CompositeActionConditionImpl(String id) 
    {
        super(id, CompositeActionCondition.COMPOSITE_CONDITION);
    }
   
    private static final long serialVersionUID = -5987435419674390938L;
   
    /**
     * The actionCondition list
     */
    private List<ActionCondition> actionConditions = new ArrayList<ActionCondition>();
   
    private static Boolean OR = true;
    private static Boolean AND = false;
   
    private Boolean AndOr = AND;
   
    public void addActionCondition(ActionCondition actionCondition) 
    {
        this.actionConditions.add(actionCondition);
    }
   
    public void addActionCondition(int index, ActionCondition actionCondition) 
    {
        this.actionConditions.add(index, actionCondition);
    }
   
    public ActionCondition getActionCondition(int index) 
    {
        return this.actionConditions.get(index);
    }
   
    public List<ActionCondition> getActionConditions() 
    {
        return this.actionConditions;
    }
   
    public boolean hasActionConditions() 
    {
        return (this.actionConditions.isEmpty() == false);
    }
   
    public int indexOfActionCondition(ActionCondition actionCondition) 
    {
        return this.actionConditions.indexOf(actionCondition);
    }
   
    public void removeActionCondition(ActionCondition actionCondition) 
    {
        this.actionConditions.remove(actionCondition);
    }
   
    public void removeAllActionConditions() 
    {
        this.actionConditions.clear();
    }
   
    public void setActionCondition(int index, ActionCondition actionCondition) 
    {
        this.actionConditions.set(index, actionCondition);
    }
   
    public boolean isORCondition() 
    {
        return AndOr;
    }
   
    public void setORCondition(boolean andOr) 
    {
        AndOr = andOr;
    }
}
