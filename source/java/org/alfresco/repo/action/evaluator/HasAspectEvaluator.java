package org.alfresco.repo.action.evaluator;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Has aspect evaluator
 * 
 * @author Roy Wetherall
 */
public class HasAspectEvaluator extends ActionConditionEvaluatorAbstractBase
{
	/**
	 * Evaluator constants
	 */
	public static final String NAME = "has-aspect";
    public static final String PARAM_ASPECT = "aspect";
	
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * Set node service
     * 
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition ruleCondition, NodeRef actionedUponNodeRef)
    {
        boolean result = false;
        
        if (this.nodeService.exists(actionedUponNodeRef) == true)
        {
            if (this.nodeService.hasAspect(actionedUponNodeRef, (QName)ruleCondition.getParameterValue(PARAM_ASPECT)) == true)
            {
                result = true;
            }
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
        paramList.add(new ParameterDefinitionImpl(PARAM_ASPECT, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_ASPECT), false, "ac-aspects"));
	}

}
