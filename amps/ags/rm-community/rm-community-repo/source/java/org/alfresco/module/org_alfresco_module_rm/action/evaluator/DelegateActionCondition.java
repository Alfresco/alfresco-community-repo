/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action.evaluator;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluator;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Records management action condition who's implementation is delegated to an existing
 * action condition.
 * <p>
 * Useful for creating a RM version of an existing action condition implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class DelegateActionCondition extends RecordsManagementActionConditionEvaluatorAbstractBase
{
    /** Delegate action evaluator */
    private ActionConditionEvaluator actionConditionEvaluator;

    /**
     * Sets the action condition evaluator
     *
     * @param actionConditionEvaluator The action condition evaluator
     */
    public void setActionConditionEvaluator(ActionConditionEvaluator actionConditionEvaluator)
    {
        this.actionConditionEvaluator = actionConditionEvaluator;
    }

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef)
    {
        return actionConditionEvaluator.evaluate(actionCondition, actionedUponNodeRef);
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#getParameterDefintions()
     */
    @Override
    protected List<ParameterDefinition> getParameterDefintions()
    {
        return actionConditionEvaluator.getActionConditionDefintion().getParameterDefinitions();
    }

}
