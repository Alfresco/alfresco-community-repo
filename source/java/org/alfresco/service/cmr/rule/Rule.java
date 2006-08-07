/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
    
    /**
     * Indicates whether the rule is applied to all the children of the associated node
     * rather than just the node itself.
     */
    private boolean isAppliedToChildren = false;
    
    public Rule()
    {
    }
    
    public Rule(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    
    public void setAction(Action action)
    {
        this.action = action;
    }
    
    public Action getAction()
    {
        return action;
    }
    
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    /**
     * @see org.alfresco.service.cmr.rule.Rule#isAppliedToChildren()
     */
    public boolean isAppliedToChildren()
    {
        return this.isAppliedToChildren;
    }
    
    /**
     *@see org.alfresco.service.cmr.rule.Rule#applyToChildren(boolean)
     */
    public void applyToChildren(boolean isAppliedToChildren)
    {
        this.isAppliedToChildren = isAppliedToChildren;
    }
    
    public void setRuleType(String ruleType)
    {
        List<String> ruleTypes = new ArrayList<String>(1);
        ruleTypes.add(ruleType);
        this.ruleTypes = ruleTypes;
    }
    
    public void setRuleTypes(List<String> ruleTypes)
    {
        this.ruleTypes = ruleTypes;
    }
    
    public List<String> getRuleTypes()
    {
        return ruleTypes;
    }
    
    public void setExecuteAsynchronously(boolean executeAsynchronously)
    {
        this.executeAsynchronously = executeAsynchronously;
    }
    
    public boolean getExecuteAsynchronously()
    {
        return this.executeAsynchronously;
    }
    
    /**
     * Hash code implementation
     */
    @Override
    public int hashCode()
    {
        return this.nodeRef.hashCode(); 
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

