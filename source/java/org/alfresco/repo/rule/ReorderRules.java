
package org.alfresco.repo.rule;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;

/**
 * Action implementation to reorder rules
 * 
 * @author Roy Wetherall
 */
public class ReorderRules extends ActionExecuterAbstractBase
{
    /** Constants */
    public static final String NAME = "reorder-rules";
    public static final String PARAM_RULES = "rules";
    
    /** Node service */
    private NodeService nodeService;
    
    /** Rule service */
    private RuleService ruleService;
    
    /**
     * Set rule service
     * 
     * @param ruleService   rule service
     */
    public void setRuleService(RuleService ruleService)
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
    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == true)
        {
            // Check that the actioned upon node has the rules aspect applied
            if (nodeService.hasAspect(actionedUponNodeRef, RuleModel.ASPECT_RULES) == true)
            {                 
                List<NodeRef> rules = (List<NodeRef>)action.getParameterValue(PARAM_RULES);
                
                int index = 0;
                for (NodeRef rule : rules)
                {
                    ruleService.setRulePosition(actionedUponNodeRef, rule, index);
                    index++;
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
        paramList.add(new ParameterDefinitionImpl(
                PARAM_RULES,
                DataTypeDefinition.NODE_REF, 
                true,
                getParamDisplayLabel(PARAM_RULES),
                true));
    }
}
