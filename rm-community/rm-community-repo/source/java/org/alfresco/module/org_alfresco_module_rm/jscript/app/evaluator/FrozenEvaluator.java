 
package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Freeze indicator.
 * <p>
 * Only shows as frozen if the user can 'read' the holds 
 * that hold the nodeRef.
 * 
 * @author Roy Wetherall
 */
public class FrozenEvaluator extends BaseEvaluator
{
    /** hold service */
    private HoldService holdService;
    
    /**
     * @param holdService   hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }
    
    /**
     * Only indicate the node is frozen if the user can 'read' at least one of the holds
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator#evaluateImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        List<NodeRef> heldBy = holdService.heldBy(nodeRef, true);
        return !heldBy.isEmpty();
    }
}
