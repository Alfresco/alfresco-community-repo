package org.alfresco.repo.action.evaluator;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;

/**
 * Has version history evaluator
 * 
 * @author Roy Wetherall
 */
public class HasVersionHistoryEvaluator extends ActionConditionEvaluatorAbstractBase
{
	/**
	 * Evaluator constants
	 */
	public static final String NAME = "has-version-history";
	
    /**
     * The node service
     */
    private NodeService nodeService;
    
    private VersionService versionService;
    
    /**
     * Set node service
     * 
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }
    
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }
    
    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition ruleCondition, NodeRef actionedUponNodeRef)
    {
        boolean result = false;
        
        if (this.nodeService.exists(actionedUponNodeRef) == true && 
            this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
        {
            VersionHistory versionHistory = this.versionService.getVersionHistory(actionedUponNodeRef);
            if (versionHistory != null && versionHistory.getAllVersions().size() != 0)
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
	}

}
