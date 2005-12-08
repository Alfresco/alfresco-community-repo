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
