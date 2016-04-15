package org.alfresco.repo.action;

import java.util.Set;

import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Rule action implementation class
 * 
 * @author Roy Wetherall
 */
public class ActionDefinitionImpl extends ParameterizedItemDefinitionImpl implements ActionDefinition
{
    private static final long serialVersionUID = 4048797883396863026L;    
    
    private String ruleActionExecutor;
    private Set<QName> applicableTypes;
    private boolean trackStatus;

    /**
     * Constructor
     * 
     * @param name  the name
     */
    public ActionDefinitionImpl(String name)
    {
        super(name);
        this.trackStatus = false;
    }
        
    /**
     * Set the rule action executor
     * 
     * @param ruleActionExecutor    the rule action executor
     */
    public void setRuleActionExecutor(String ruleActionExecutor)
    {
        this.ruleActionExecutor = ruleActionExecutor;
    }
    
    /**
     * Get the rule aciton executor
     * 
     * @return  the rule action executor
     */
    public String getRuleActionExecutor()
    {
        return ruleActionExecutor;
    }
    
    /**
     * Gets the set of applicable types
     * 
     * @return  the set of qnames
     */
    public Set<QName> getApplicableTypes()
    {
        return this.applicableTypes;
    }
    
    /**
     * Sets the list of applicable types
     * 
     * @param applicableTypes   the applicable types
     */
    public void setApplicableTypes(Set<QName> applicableTypes)
    {
        this.applicableTypes = applicableTypes;
    }

    @Override
    public boolean getTrackStatus()
    {
        return trackStatus;
    }

    /**
     * Set whether the basic action definition requires status tracking.
     * This can be overridden on each action instance but if not, it falls back
     * to this definition.
     * <p/>
     * Setting this to <tt>true</tt> introduces performance problems for concurrently-executing
     * rules on V3.4: <a href="https://issues.alfresco.com/jira/browse/ALF-7341">ALF-7341</a>.
     * 
     * @param trackStatus           <tt>true</tt> to track execution status otherwise <tt>false</tt>
     * 
     * @since 3.4.1
     */
    public void setTrackStatus(boolean trackStatus)
    {
        this.trackStatus = trackStatus;
    }
}
