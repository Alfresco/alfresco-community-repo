/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
