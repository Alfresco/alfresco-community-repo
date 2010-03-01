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
package org.alfresco.repo.action.evaluator;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action Condition Evaluator
 * 
 * @author Roy Wetherall
 */
public interface ActionConditionEvaluator
{
    /**
     * Get the action condition deinfinition
     * 
     * @return  the action condition definition
     */
	public ActionConditionDefinition getActionConditionDefintion();
	
    /**
     * Evaluate the action condition
     * 
     * @param actionCondition       the action condition
     * @param actionedUponNodeRef   the actioned upon node
     * @return                      true if the condition passes, false otherwise
     */
    public boolean evaluate(
			ActionCondition actionCondition,
            NodeRef actionedUponNodeRef);
}
