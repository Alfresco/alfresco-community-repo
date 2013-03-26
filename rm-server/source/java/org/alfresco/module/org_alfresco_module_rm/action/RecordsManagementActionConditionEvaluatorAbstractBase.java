/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action;

import java.util.List;

import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;

/**
 * Records management action condition evaluator abstract base implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class RecordsManagementActionConditionEvaluatorAbstractBase extends ActionConditionEvaluatorAbstractBase
{
    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#getActionConditionDefintion()
     * 
     * TODO base class should provide "createActionDefinition" method that can be over-ridden like the ActionExecuter 
     * base class to prevent duplication of code and a cleaner extension.
     */
    @Override
    public ActionConditionDefinition getActionConditionDefintion()
    {
        if (this.actionConditionDefinition == null)
        {
            this.actionConditionDefinition = new RecordsManagementActionConditionDefinitionImpl(name);
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setTitleKey(getTitleKey());
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setDescriptionKey(getDescriptionKey());
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setAdhocPropertiesAllowed(getAdhocPropertiesAllowed());
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setConditionEvaluator(name);
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setParameterDefinitions(getParameterDefintions());
        }
        return this.actionConditionDefinition;
    }
    
    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> arg0)
    {
        // No param implementation by default
    }

}
