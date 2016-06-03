
package org.alfresco.repo.rule;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;

/**
 * Action implementation to unlink the rules from one folder to another
 * 
 * @author Roy Wetherall
 */
public class UnlinkRules extends ActionExecuterAbstractBase
{
    /** Constants */
    public static final String NAME = "unlink-rules";
    
    /** Node service */
    private NodeService nodeService;
    
    /** Runtime rule service */
    private RuntimeRuleService ruleService;
    
    /**
     * Set rule service
     * 
     * @param ruleService   rule service
     */
    public void setRuleService(RuntimeRuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * Set node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == true)
        {
            // Check that the actioned upon node has the rules aspect applied
            if (nodeService.hasAspect(actionedUponNodeRef, RuleModel.ASPECT_RULES) == true)
            {                 
                // Get the rule node the actioned upon node is linked to
                NodeRef linkedToNode = ((RuleService)ruleService).getLinkedToRuleNode(actionedUponNodeRef);
                if (linkedToNode != null)
                {
                    nodeService.removeAspect(actionedUponNodeRef, RuleModel.ASPECT_RULES);
                }
            }
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
