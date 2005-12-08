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
package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Add features action executor implementation.
 * 
 * @author Roy Wetherall
 */
public class CompositeActionExecuter extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
	public static final String NAME = "composite-action";
	
	/**
	 * The action service
	 */
	private RuntimeActionService actionService;
	
	/**
	 * Set the action service
	 * 
	 * @param actionService  the action service
	 */
	public void setActionService(RuntimeActionService actionService)
	{
		this.actionService = actionService;
	}

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef)
     */
    public void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
    	if (action instanceof CompositeAction)
		{
			for (Action subAction : ((CompositeAction)action).getActions())
			{
				// We don't check the conditions of sub-actions and they don't have an execution history
				this.actionService.directActionExecution(subAction, actionedUponNodeRef);
			}
		}
    }

    /**
     * Add parameter definitions
     */
	@Override
	protected void addParameterDefintions(List<ParameterDefinition> paramList) 
	{
		// No parameters
	}

}
