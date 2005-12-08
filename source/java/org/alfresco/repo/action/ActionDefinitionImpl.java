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
package org.alfresco.repo.action;

import org.alfresco.service.cmr.action.ActionDefinition;

/**
 * Rule action implementation class
 * 
 * @author Roy Wetherall
 */
public class ActionDefinitionImpl extends ParameterizedItemDefinitionImpl
                            implements ActionDefinition
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 4048797883396863026L;    
    
    /**
     * The rule action executor
     */
    private String ruleActionExecutor;

    /**
     * Constructor
     * 
     * @param name  the name
     */
    public ActionDefinitionImpl(String name)
    {
        super(name);
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
}
