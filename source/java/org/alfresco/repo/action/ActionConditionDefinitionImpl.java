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
