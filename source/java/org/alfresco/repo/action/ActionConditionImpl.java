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
     * Rule condition defintion
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
     * @param parameterValues
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
