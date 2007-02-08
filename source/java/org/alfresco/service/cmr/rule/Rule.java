/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Rule class.
 * <p>
 * Encapsulates all the information about a rule.  Can be creted or editied and
 * then passed to the rule service to create/update a rule instance.
 * 
 * @author Roy Wetherall
 */
public class Rule implements Serializable
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 3544385898889097524L;

    /**
     * The rule node reference
     */
    private NodeRef nodeRef;
    
    /**
     * The title of the rule
     */
    private String title;
    
    /**
     * The description of the rule
     */
    private String description;
    
    /**
	 * The rule types
	 */
	private List<String> ruleTypes;
    
    /**
     * The associated action
     */
    private Action action;  
    
    /**
     * Indicates whether the rule should execute the action asynchronously or not
     */
    private boolean executeAsynchronously = false;
    
    /** Indicates wehther the rule is marked as disabled or not */
    private boolean ruleDisabled = false;
    
    /**
     * Indicates whether the rule is applied to all the children of the associated node
     * rather than just the node itself.
     */
    private boolean isAppliedToChildren = false;
    
    /**
     * Constructor
     */
    public Rule()
    {
    }
    
    /**
     * Constructor.
     * 
     * @param nodeRef  the rule node reference
     */
    public Rule(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    
    /**
     * Set the action
     * 
     * @param action    the action
     */
    public void setAction(Action action)
    {
        this.action = action;
    }
    
    /**
     * Gets the action associatied with the rule
     * 
     * @return  the action
     */
    public Action getAction()
    {
        return action;
    }
    
    /**
     * Set the node reference of the rule
     * 
     * @param nodeRef   the rule node reference
     */
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    
    /**
     * Get the node reference of the rule
     * 
     * @return  the rule node reference
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    /**
     * Set the title of the rule
     * 
     * @param title the title 
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    /**
     * Get the title of the rule
     * 
     * @return  the title
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * Set the description of the rule 
     * 
     * @param description   the description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * Get the description of the rule
     * 
     * @return  the description
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     *  Indicates wehther this rule should be applied to the children of 
     *  the owning space.
     * 
     * @return  true if the rule is to be applied to children, false otherwise   
     */
    public boolean isAppliedToChildren()
    {
        return this.isAppliedToChildren;
    }
    
    /**
     * Sets the values that indicates whether this rule should be applied to the children
     * of the owning space.
     * 
     * @param isAppliedToChildren   true if the rule is to be applied to children, false otherwise
     */
    
    public void applyToChildren(boolean isAppliedToChildren)
    {
        this.isAppliedToChildren = isAppliedToChildren;
    }
    
    /**
     * Helper method to set one rule type on the rule.
     * 
     * @param ruleType  the rule type
     */
    public void setRuleType(String ruleType)
    {
        List<String> ruleTypes = new ArrayList<String>(1);
        ruleTypes.add(ruleType);
        this.ruleTypes = ruleTypes;
    }
    
    /**
     * Set the rules rule types.
     * 
     * @param ruleTypes list of rule types
     */
    public void setRuleTypes(List<String> ruleTypes)
    {
        this.ruleTypes = ruleTypes;
    }
    
    /**
     * Get the rules rule types.
     * 
     * @return  a list of rule types
     */
    public List<String> getRuleTypes()
    {
        return ruleTypes;
    }
    
    /**
     * Sets the value that indicates whether this associated action should be executed
     * asynchrously or not
     * 
     * @param executeAsynchronously true to execute action async, false otherwise
     */
    public void setExecuteAsynchronously(boolean executeAsynchronously)
    {
        this.executeAsynchronously = executeAsynchronously;
    }
    
    /**
     * Indicates whether the associated action should be executed async or not
     * 
     * @return  true to execute async, false otherwise
     */
    public boolean getExecuteAsynchronously()
    {
        return this.executeAsynchronously;
    }
    
    /**
     * Indicates wehther this rule has been disabled or not
     * 
     * @return  true if the rule has been disabled, false otherwise
     */
    public boolean getRuleDisabled()
    {
        return this.ruleDisabled;
    }
    
    /**
     * Set the value that indicates wehther this rule has been disabled or not
     * 
     * @param ruleDisabled  true id the rule has been disabled, false otherwise
     */
    public void setRuleDisabled(boolean ruleDisabled)
    {
        this.ruleDisabled = ruleDisabled;
    }
    
    /**
     * Hash code implementation
     */
    @Override
    public int hashCode()
    {
        return (this.nodeRef == null) ? 0 : this.nodeRef.hashCode(); 
    }
    
    /**
     * Equals implementation
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof Rule)
        {
            Rule that = (Rule) obj;
            return (this.nodeRef.equals(that.nodeRef));
        }
        else
        {
            return false;
        }
    }
}

