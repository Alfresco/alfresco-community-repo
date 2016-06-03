package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Action executer that counts the number of children the actioned upon node has.
 * This provides as example of how actions can return results.
 * 
 * @author Roy Wetherall
 */
public class CountChildrenActionExecuter extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
	public static final String NAME = "count-children";
	
	/**
	 * The node service
	 */
	private NodeService nodeService;
	
    /**
     * Set the node service
     * 
     * @param nodeService  the node service
     */
	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(Action, NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
		if (this.nodeService.exists(actionedUponNodeRef) == true)
		{
			// Get the parent node		
			int count = this.nodeService.getChildAssocs(actionedUponNodeRef).size();
			ruleAction.setParameterValue(PARAM_RESULT, Integer.valueOf(count));
		}
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{		
	}

}
