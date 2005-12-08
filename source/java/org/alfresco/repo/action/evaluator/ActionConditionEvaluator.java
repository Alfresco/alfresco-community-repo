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
