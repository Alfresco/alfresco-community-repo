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
package org.alfresco.repo.rule;

import java.io.Serializable;

import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.util.ParameterCheck;

/**
 * Rule implementation class.
 * <p>
 * Encapsulates all the information about a rule.  Can be creted or editied and
 * then passed to the rule service to create/update a rule instance.
 * 
 * @author Roy Wetherall
 */
public class RuleImpl extends CompositeActionImpl implements Serializable, Rule 
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 3544385898889097524L;

    /**
	 * The rule type name
	 */
	private String ruleTypeName;
    
    /**
     * Indicates whether the rule is applied to all the children of the associated node
     * rather than just the node itself.
     */
    private boolean isAppliedToChildren = false;
    
	/**
	 * Constructor
	 * 
	 * @param ruleTypeName	the rule type name
	 */
	public RuleImpl(String id, String ruleTypeName, NodeRef owningNodeRef)
	{
		super(id, owningNodeRef);		
		ParameterCheck.mandatory("ruleTypeName", ruleTypeName);
		
		this.ruleTypeName = ruleTypeName;
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
    
	/**
     * @see org.alfresco.service.cmr.rule.Rule#getRuleTypeName()
     */
	public String getRuleTypeName()
	{
		return this.ruleTypeName;
	}
}

