package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;

/**
 * Take Ownership action executor
 * 
 * @author Neil Mc Erlean
 * @since 5.0
 */
public class TakeOwnershipActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "take-ownership";
    
    private NodeService    nodeService;
    private OwnableService ownableService;
    
    public void setNodeService   (NodeService service)    { this.nodeService    = service; }
    public void setOwnableService(OwnableService service) { this.ownableService = service; }
    
    @Override protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        // Intentionally empty. There are no parameters for this action.
    }
    
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) && isApplicableType(actionedUponNodeRef))
        {
            ownableService.takeOwnership(actionedUponNodeRef);
        }
    }
}
