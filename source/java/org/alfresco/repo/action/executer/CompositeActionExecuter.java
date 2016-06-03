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
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(Action, NodeRef)
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
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		// No parameters
	}

}
